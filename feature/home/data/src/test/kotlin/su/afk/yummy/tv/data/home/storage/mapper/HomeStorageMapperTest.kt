package su.afk.yummy.tv.data.home.storage.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import su.afk.yummy.tv.core.error.api.StringProvider
import su.afk.yummy.tv.data.home.dto.YaniAnimeDto
import su.afk.yummy.tv.data.home.dto.YaniFeedDto
import su.afk.yummy.tv.data.home.dto.YaniFeedResponseDto
import su.afk.yummy.tv.data.home.dto.YaniVideoDto
import su.afk.yummy.tv.core.storage.home.HOME_FEED_CONTAINER_NEW_RELEASES
import su.afk.yummy.tv.domain.home.model.HomeFeedSectionType

class HomeStorageMapperTest {

    private val stringProvider: StringProvider = mockk {
        every { get(any<Int>()) } returns "title"
    }

    @Test
    fun `announcements and new titles become their own sections`() {
        val feed = YaniFeedDto(
            YaniFeedResponseDto(
                announcements = listOf(anime(1), anime(2)),
                new = listOf(anime(3)),
                recommends = listOf(anime(4)),
            ),
        ).toHomeFeedCache(language = "ru", watchSignature = "", cachedAt = 0L)
            .toHomeFeed(stringProvider)

        assertEquals(
            listOf(
                HomeFeedSectionType.NEW_TITLES,
                HomeFeedSectionType.RECOMMENDATIONS,
                HomeFeedSectionType.ANNOUNCEMENTS,
            ),
            feed.sections.map { it.type },
        )
        assertEquals(
            listOf(1, 2),
            feed.sections.single { it.type == HomeFeedSectionType.ANNOUNCEMENTS }.items.map { it.id },
        )
        assertEquals(
            listOf(3),
            feed.sections.single { it.type == HomeFeedSectionType.NEW_TITLES }.items.map { it.id },
        )
    }


    @Test
    fun `late upload of an older episode is excluded when a newer episode is already cached`() {
        val cache = YaniFeedDto(
            YaniFeedResponseDto(
                newVideos = listOf(
                    YaniVideoDto(animeId = 10, title = "Anime", episodeTitle = "6", videoId = 60),
                ),
            ),
        ).toHomeFeedCache(
            language = "ru",
            watchSignature = "",
            cachedAt = 0L,
            availableEpisodeCounts = mapOf(10 to 8),
        )

        assertEquals(
            emptyList<Int>(),
            cache.items.filter { it.container == HOME_FEED_CONTAINER_NEW_RELEASES }.map { it.itemId },
        )
    }

    @Test
    fun `new release selects the highest regular episode instead of feed order`() {
        val cache = YaniFeedDto(
            YaniFeedResponseDto(
                newVideos = listOf(
                    YaniVideoDto(animeId = 10, title = "Anime", episodeTitle = "6", videoId = 60),
                    YaniVideoDto(animeId = 10, title = "Anime", episodeTitle = "8", videoId = 80),
                ),
            ),
        ).toHomeFeedCache(language = "ru", watchSignature = "", cachedAt = 0L)

        assertEquals(
            listOf(10),
            cache.items.filter { it.container == HOME_FEED_CONTAINER_NEW_RELEASES }.map { it.itemId },
        )
    }

    private fun anime(id: Int) = YaniAnimeDto(animeId = id, title = "Anime $id")
}
