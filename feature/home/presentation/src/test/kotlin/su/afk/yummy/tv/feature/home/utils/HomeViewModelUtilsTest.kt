package su.afk.yummy.tv.feature.home.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import su.afk.yummy.tv.core.testing.BaseUnitTest
import su.afk.yummy.tv.domain.home.model.HomeFeed
import su.afk.yummy.tv.domain.home.model.HomeFeedItem
import su.afk.yummy.tv.domain.home.model.HomeFeedItemAction
import su.afk.yummy.tv.domain.home.model.HomeFeedSection
import su.afk.yummy.tv.domain.home.model.HomeFeedSectionType

class HomeViewModelUtilsTest : BaseUnitTest() {

    @Test
    fun `new releases inherit episode number and publication time`() {
        val original = feed()
        val aired = airedItem(50, episodeNumber = 7).copy(airedAtSeconds = 1_800_000_000L)

        val result = original.withRecentReleaseMetadata(listOf(aired))

        val item = result.sections.first().items.single()
        assertEquals(7, item.episodeNumber)
        assertEquals(1_800_000_000L, item.airedAtSeconds)
    }

    @Test
    fun `watched episodes are hidden from ordinary new releases`() {
        val original = feed()
        val result = original.withRecentReleaseMetadata(
            recentlyAired = emptyList(),
            watchedEpisodes = mapOf(50 to setOf(1)),
        )

        assertEquals(emptyList<Int>(), result.sections.first().items.map { it.id })
    }

    @Test
    fun `unwatched ordinary new releases keep their original order`() {
        val original = HomeFeed(
            heroItems = emptyList(),
            sections = listOf(
                HomeFeedSection(
                    HomeFeedSectionType.NEW_RELEASES,
                    "New",
                    listOf(airedItem(1, 2), airedItem(2, 3), airedItem(3, 1)),
                ),
            ),
        )
        val result = original.withRecentReleaseMetadata(
            recentlyAired = emptyList(),
            watchedEpisodes = mapOf(1 to setOf(2), 3 to setOf(1)),
        )

        assertEquals(listOf(2), result.sections.first().items.map { it.id })
    }

    @Test
    fun `selected library titles are highlighted without changing ordinary new release order`() {
        val original = HomeFeed(
            heroItems = emptyList(),
            sections = listOf(
                HomeFeedSection(
                    HomeFeedSectionType.NEW_RELEASES,
                    "New",
                    listOf(airedItem(1), airedItem(2), airedItem(3)),
                ),
            ),
        )

        val result = original.withRecentReleaseMetadata(
            recentlyAired = emptyList(),
            prioritizedAnimeIds = setOf(2, 3),
        )

        val items = result.sections.single().items
        assertEquals(listOf(1, 2, 3), items.map { it.id })
        assertEquals(listOf(false, true, true), items.map { it.isFromSelectedList })
    }

    @Test
    fun `only titles from the library reach the section`() {
        val feed = feed().withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1), airedItem(2)),
            libraryAnimeIds = setOf(2),
            watchedEpisodes = emptyMap(),
            title = TITLE,
        )

        assertEquals(listOf(2), feed.sections.first().items.map { it.id })
        assertEquals(true, feed.sections.first().items.single().isFromSelectedList)
    }

    @Test
    fun `the section goes first in the feed`() {
        val feed = feed().withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1)),
            libraryAnimeIds = setOf(1),
            watchedEpisodes = emptyMap(),
            title = TITLE,
        )

        assertEquals(HomeFeedSectionType.MY_NEW_EPISODES, feed.sections.first().type)
        assertEquals(TITLE, feed.sections.first().title)
        assertEquals(listOf(HomeFeedSectionType.NEW_RELEASES), feed.sections.drop(1).map { it.type })
    }

    @Test
    fun `the feed is untouched when nothing matches`() {
        val original = feed()

        val result = original.withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1)),
            libraryAnimeIds = setOf(999),
            watchedEpisodes = emptyMap(),
            title = TITLE,
        )

        assertEquals(original, result)
    }

    @Test
    fun `a watched episode is hidden from personalized new releases`() {
        val original = feed()
        val result = original.withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1, episodeNumber = 3)),
            libraryAnimeIds = setOf(1),
            watchedEpisodes = mapOf(1 to setOf(1, 2, 3)),
            title = TITLE,
        )

        assertEquals(HomeFeedSectionType.NEW_RELEASES, result.sections.first().type)
    }

    @Test
    fun `an earlier watched episode does not mark the new one`() {
        val feed = feed().withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1, episodeNumber = 3)),
            libraryAnimeIds = setOf(1),
            watchedEpisodes = mapOf(1 to setOf(1, 2)),
            title = TITLE,
        )

        assertFalse(feed.sections.first().items.single().isWatched)
    }

    @Test
    fun `a title without an episode number is never marked watched`() {
        val feed = feed().withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1, episodeNumber = null)),
            libraryAnimeIds = setOf(1),
            watchedEpisodes = mapOf(1 to setOf(1, 2, 3)),
            title = TITLE,
        )

        assertFalse(feed.sections.first().items.single().isWatched)
    }

    @Test
    fun `only unwatched releases remain visible in personalized section`() {
        val feed = feed().withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1, episodeNumber = 3), airedItem(2, episodeNumber = 1)),
            libraryAnimeIds = setOf(1, 2),
            watchedEpisodes = mapOf(1 to setOf(3)),
            title = TITLE,
        )

        assertEquals(listOf(2), feed.sections.first().items.map { it.id })
    }

    @Test
    fun `the personalized section is omitted when every new release was watched`() {
        val original = feed()

        val result = original.withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1, episodeNumber = 3)),
            libraryAnimeIds = setOf(1),
            watchedEpisodes = mapOf(1 to setOf(3)),
            title = TITLE,
        )

        assertEquals(HomeFeedSectionType.NEW_RELEASES, result.sections.first().type)
    }

    @Test
    fun `titles whose new episode is still unwatched stay`() {
        val feed = feed().withMyNewEpisodes(
            recentlyAired = listOf(airedItem(1, episodeNumber = 3)),
            libraryAnimeIds = setOf(1),
            watchedEpisodes = mapOf(1 to setOf(1, 2)),
            title = TITLE,
        )

        assertEquals(listOf(1), feed.sections.first().items.map { it.id })
    }

    private fun feed() = HomeFeed(
        heroItems = emptyList(),
        sections = listOf(
            HomeFeedSection(HomeFeedSectionType.NEW_RELEASES, "New", listOf(airedItem(50))),
        ),
    )

    private fun airedItem(id: Int, episodeNumber: Int? = 1) = HomeFeedItem(
        id = id,
        title = "Title $id",
        description = "",
        poster = null,
        rating = null,
        year = null,
        action = HomeFeedItemAction.OpenSeries(id),
        episodeNumber = episodeNumber,
        airedAtSeconds = 1_791_000_000L,
    )

    private companion object {
        const val TITLE = "New episodes"
    }
}
