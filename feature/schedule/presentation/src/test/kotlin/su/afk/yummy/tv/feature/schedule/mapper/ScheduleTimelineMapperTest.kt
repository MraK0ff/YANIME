package su.afk.yummy.tv.feature.schedule.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleItem
import java.time.Instant
import java.time.ZoneId

class ScheduleTimelineMapperTest {

    private val zone = ZoneId.of("UTC")
    private val now = Instant.parse("2026-11-14T12:00:00Z").atZone(zone)
    private val nowSeconds = now.toEpochSecond()

    @Test
    fun `past Japanese previous date is not shown without a published video`() {
        val timeline = listOf(day(item(previousDate = nowSeconds - DAY))).toTimelineUi(zone, now)

        assertTrue(timeline.dayGroups.isEmpty())
    }

    @Test
    fun `published episode uses the first YummyAnime video timestamp`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    previousDate = nowSeconds - 2 * DAY,
                    releases = listOf(release(episode = 4, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        val release = timeline.dayGroups.single().items.single()
        assertEquals(4, release.episode)
        assertEquals(publishedAt, release.epochSeconds)
        assertTrue(release.aired)
    }

    @Test
    fun `future schedule remains visible and is not marked aired`() {
        val future = nowSeconds + DAY
        val timeline = listOf(day(item(nextDate = future, airedEpisodes = 4))).toTimelineUi(zone, now)

        val release = timeline.dayGroups.single().items.single()
        assertEquals(5, release.episode)
        assertEquals(future, release.epochSeconds)
        assertFalse(release.aired)
    }

    @Test
    fun `multiple actually published episodes remain separate calendar entries`() {
        val first = nowSeconds - 2 * 3_600L
        val second = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    releases = listOf(
                        release(episode = 3, publishedAt = first),
                        release(episode = 4, publishedAt = second),
                    ),
                ),
            ),
        ).toTimelineUi(zone, now)

        assertEquals(listOf(3, 4), timeline.dayGroups.flatMap { it.items }.map { it.episode })
        assertEquals(listOf(first, second), timeline.dayGroups.flatMap { it.items }.map { it.epochSeconds })
    }

    @Test
    fun `episodes with the same publication timestamp have unique lazy list keys`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    releases = listOf(
                        release(episode = 3, publishedAt = publishedAt),
                        release(episode = 4, publishedAt = publishedAt),
                    ),
                ),
            ),
        ).toTimelineUi(zone, now)

        val releases = timeline.dayGroups.single().items
        assertEquals(2, releases.size)
        assertEquals(2, releases.map { it.focusKey }.distinct().size)
    }


    @Test
    fun `duplicate upstream rows cannot produce duplicate lazy keys across the timeline`() {
        val publishedAt = nowSeconds - 3_600L
        val duplicate = item(
            releases = listOf(
                release(episode = 3, publishedAt = publishedAt),
                release(episode = 4, publishedAt = publishedAt),
            ),
        )
        val timeline = listOf(
            day(duplicate),
            day(duplicate),
        ).toTimelineUi(zone, now)

        val releases = timeline.dayGroups.flatMap { it.items }
        assertEquals(2, releases.size)
        assertEquals(releases.size, releases.map { it.focusKey }.distinct().size)
        assertEquals(setOf("1:3:true", "1:4:true"), releases.map { it.focusKey }.toSet())
    }

    @Test
    fun `duplicate episode records keep the earliest publication timestamp`() {
        val firstPublishedAt = nowSeconds - 7_200L
        val laterDuplicateAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    releases = listOf(
                        release(episode = 3, publishedAt = laterDuplicateAt),
                        release(episode = 3, publishedAt = firstPublishedAt),
                    ),
                ),
            ),
        ).toTimelineUi(zone, now)

        val release = timeline.dayGroups.single().items.single()
        assertEquals(3, release.episode)
        assertEquals(firstPublishedAt, release.epochSeconds)
        assertEquals("1:3:true", release.focusKey)
    }

    private fun day(item: AnimeScheduleItem) = AnimeScheduleDay("day", listOf(item))

    private fun item(
        previousDate: Long? = null,
        nextDate: Long? = null,
        airedEpisodes: Int? = 4,
        releases: List<AnimeEpisodeRelease> = emptyList(),
    ) = AnimeScheduleItem(
        animeId = 1,
        title = "Anime",
        poster = null,
        nextDateEpochSeconds = nextDate,
        previousDateEpochSeconds = previousDate,
        airedEpisodes = airedEpisodes,
        totalEpisodes = 12,
        videoReleases = releases,
    )

    private fun release(episode: Int, publishedAt: Long) = AnimeEpisodeRelease(
        animeId = 1,
        episodeNumber = episode,
        firstVideoAtSeconds = publishedAt,
        firstVideoId = episode,
        title = "Anime",
    )

    private companion object {
        const val DAY = 24L * 60 * 60
    }
}
