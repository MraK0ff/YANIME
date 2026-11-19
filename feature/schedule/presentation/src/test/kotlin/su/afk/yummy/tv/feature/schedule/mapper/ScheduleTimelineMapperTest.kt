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
    fun `current episode video removes the waiting schedule entry`() {
        val broadcastAt = nowSeconds - 60L * 60L
        val publishedAt = nowSeconds - 5L * 60L
        val timeline = listOf(
            day(
                item(
                    previousDate = broadcastAt,
                    releases = listOf(release(episode = 3, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        val release = timeline.dayGroups.single().items.single()
        assertEquals(3, release.episode)
        assertEquals(publishedAt, release.epochSeconds)
        assertTrue(release.aired)
    }

    @Test
    fun `current aired episode is grouped by Japanese broadcast date not first feed observation`() {
        val japaneseBroadcastAt = Instant.parse("2026-11-13T20:00:00Z").epochSecond
        val firstObservedAt = nowSeconds - 60L
        val timeline = listOf(
            day(
                item(
                    previousDate = japaneseBroadcastAt,
                    airedEpisodes = 7,
                    releases = listOf(release(episode = 7, publishedAt = firstObservedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        // The Japanese broadcast was on Nov 13, so it must not be shown as a Nov 14 event.
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
    fun `local cache does not reject the current aired episode`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 8,
                    availableEpisodeCount = 8,
                    releases = listOf(release(episode = 7, publishedAt = publishedAt)),
                ),
            ),
        ).toTimelineUi(zone, now)

        assertTrue(timeline.dayGroups.isEmpty())
    }

    @Test
    fun `local page cache does not hide current aired release`() {
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

        assertEquals(8, timeline.dayGroups.single().items.single().episode)
    }

    @Test
    fun `calendar keeps only the newest eligible episode for an anime`() {
        val olderAt = nowSeconds - 7_200L
        val latestAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(
                item(
                    airedEpisodes = 7,
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
    fun `selected library titles do not change chronological order within a schedule day`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            AnimeScheduleDay(
                "day",
                listOf(
                    item(animeId = 1, releases = listOf(release(episode = 2, publishedAt = publishedAt))),
                    item(animeId = 2, releases = listOf(release(episode = 3, publishedAt = publishedAt))),
                    item(animeId = 3, releases = listOf(release(episode = 4, publishedAt = publishedAt))),
                ),
            ),
        ).toTimelineUi(zone, now, prioritizedAnimeIds = setOf(2, 3))

        val releases = timeline.dayGroups.single().items
        assertEquals(listOf(1, 2, 3), releases.map { it.item.animeId })
        assertEquals(listOf(false, true, true), releases.map { it.isFromSelectedList })
    }

    @Test
    fun `favorite state is exposed independently from list membership`() {
        val publishedAt = nowSeconds - 3_600L
        val timeline = listOf(
            day(item(releases = listOf(release(episode = 4, publishedAt = publishedAt)))),
        ).toTimelineUi(zone, now, favoriteAnimeIds = setOf(1))

        val release = timeline.dayGroups.single().items.single()
        assertTrue(release.isFavorite)
        assertFalse(release.isFromSelectedList)
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
                    airedEpisodes = 2,
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
        animeId: Int = 1,
        previousDate: Long? = null,
        nextDate: Long? = null,
        airedEpisodes: Int? = 3,
        availableEpisodeCount: Int? = null,
        releases: List<AnimeEpisodeRelease> = emptyList(),
    ) = AnimeScheduleItem(
        animeId = animeId,
        title = "Anime $animeId",
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
