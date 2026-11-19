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
    fun `late dub of an episode older than the aired count is not a calendar event`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 8,
                    releases = listOf(release(episode = 6, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        assertTrue(timeline.dayGroups.isEmpty())
    }

    @Test
    fun `cached anime page count blocks a late dub despite stale schedule count`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 6,
                    availableEpisodeCount = 8,
                    releases = listOf(release(episode = 6, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        assertTrue(timeline.dayGroups.isEmpty())
    }

    @Test
    fun `late dub of an episode already in local cache is not a calendar event`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 8,
                    availableEpisodeCount = 8,
                    releases = listOf(release(episode = 8, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        assertTrue(timeline.dayGroups.isEmpty())
    }

    @Test
    fun `calendar keeps only the newest eligible episode for an anime`() {
        val olderAt = nowSeconds - 7_200L
        val latestAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 8,
                    releases = listOf(
                        release(episode = 6, publishedAt = olderAt),
                        release(episode = 8, publishedAt = latestAt),
                    ),
                ),
            ),
        ).toTimelineUi(zone, now)

        val release = timeline.dayGroups.single().items.single()
        assertEquals(8, release.episode)
        assertEquals(latestAt, release.epochSeconds)
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
    fun `calendar shows only the latest published episode per anime and day`() {
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

        val release = timeline.dayGroups.single().items.single()
        assertEquals(4, release.episode)
        assertEquals(second, release.epochSeconds)
    }

    @Test
    fun `episodes for one anime with the same timestamp collapse to the latest episode`() {
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
        assertEquals(1, releases.size)
        assertEquals(4, releases.single().episode)
    }


    @Test
    fun `duplicate anime rows collapse to one entry per day`() {
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
        assertEquals(1, releases.size)
        assertEquals(4, releases.single().episode)
    }

    @Test
    fun `future scheduled episode wins over same-day historical releases`() {
        val publishedAt = nowSeconds - 3_600L
        val futureAt = nowSeconds + 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 4,
                    nextDate = futureAt,
                    releases = listOf(release(episode = 4, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        val release = timeline.dayGroups.single().items.single()
        assertEquals(5, release.episode)
        assertEquals(futureAt, release.epochSeconds)
        assertFalse(release.aired)
    }

    @Test
    fun `duplicate episode records keep the earliest publication timestamp`() {
        val firstPublishedAt = nowSeconds - 7_200L
        val laterDuplicateAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 3,
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
        availableEpisodeCount: Int? = null,
        releases: List<AnimeEpisodeRelease> = emptyList(),
    ) = AnimeScheduleItem(
        animeId = 1,
        title = "Anime",
        poster = null,
        nextDateEpochSeconds = nextDate,
        previousDateEpochSeconds = previousDate,
        airedEpisodes = airedEpisodes,
        totalEpisodes = 12,
        availableEpisodeCount = availableEpisodeCount,
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
