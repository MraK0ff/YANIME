package su.afk.yummy.tv.data.home.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import su.afk.yummy.tv.core.storage.home.HomeNewReleaseEntry
import su.afk.yummy.tv.data.home.dto.YaniVideoDto

class YaniNewReleaseTrackerTest {

    private val tracker = YaniNewReleaseTracker()

    @Test
    fun `initial snapshot creates a stable baseline without promoting cards`() {
        val result = tracker.merge(
            videos = listOf(video(1, "10"), video(2, "4")),
            history = emptyList(),
            now = 100L,
        )

        assertEquals(listOf(1, 2), result.map { it.animeId })
        assertTrue(result.all { it.lastUpdatedAt == 0L })
    }

    @Test
    fun `only an increased regular episode promotes the anime`() {
        val baseline = tracker.merge(
            videos = listOf(video(1, "10"), video(2, "4")),
            history = emptyList(),
            now = 100L,
        )

        val updated = tracker.merge(
            videos = listOf(video(2, "5"), video(1, "10")),
            history = baseline,
            now = 200L,
        )

        assertEquals(listOf(2, 1), updated.map { it.animeId })
        assertEquals(200L, updated.first().lastUpdatedAt)
        assertEquals(0L, updated.last().lastUpdatedAt)
    }

    @Test
    fun `alternate dub of known episode does not promote or replace upload`() {
        val baseline = tracker.merge(listOf(video(1, "10", dub = "A", videoId = 11)), emptyList(), 100L)
        val updated = tracker.merge(
            listOf(video(1, "10", dub = "B", videoId = 22)),
            baseline,
            200L,
        )

        assertEquals(baseline.map { it.animeId }, updated.map { it.animeId })
        assertEquals(11, updated.single().videoId)
        assertEquals(0L, updated.single().lastUpdatedAt)
    }

    @Test
    fun `older repost and duplicate records never move a card`() {
        val baseline = tracker.merge(listOf(video(1, "10"), video(2, "5")), emptyList(), 100L)
        val updated = tracker.merge(
            listOf(video(1, "9"), video(1, "10"), video(1, "10", dub = "Other")),
            baseline,
            200L,
        )

        assertEquals(baseline.map { it.animeId }, updated.map { it.animeId })
        assertEquals(10, updated.first { it.animeId == 1 }.episodeNumber)
    }

    @Test
    fun `older episode upload is ignored when the schedule already passed it`() {
        val baseline = tracker.merge(listOf(video(1, "5")), emptyList(), 100L)
        val updated = tracker.merge(
            videos = listOf(video(1, "6", dub = "Late dub", videoId = 66)),
            history = baseline,
            now = 200L,
            airedEpisodeCounts = mapOf(1 to 8),
        )

        assertTrue(updated.isEmpty())
    }

    @Test
    fun `stale history card is removed even when the feed is empty`() {
        val baseline = tracker.merge(listOf(video(1, "6")), emptyList(), 100L)
        val updated = tracker.merge(
            videos = emptyList(),
            history = baseline,
            now = 200L,
            airedEpisodeCounts = mapOf(1 to 8),
        )

        assertTrue(updated.isEmpty())
    }

    @Test
    fun `untracked old episode is not added as a new release`() {
        val result = tracker.merge(
            videos = listOf(video(1, "6")),
            history = emptyList(),
            now = 200L,
            airedEpisodeCounts = mapOf(1 to 8),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `episode equal to aired count does not promote an existing title`() {
        val baseline = tracker.merge(listOf(video(1, "7")), emptyList(), 100L)
        val updated = tracker.merge(
            videos = listOf(video(1, "8", dub = "Alternate dub", videoId = 88)),
            history = baseline,
            now = 200L,
            airedEpisodeCounts = mapOf(1 to 8),
        )

        assertTrue(updated.isEmpty())
    }

    @Test
    fun `episode beyond the known available count is promoted`() {
        val baseline = tracker.merge(listOf(video(1, "8")), emptyList(), 100L)
        val updated = tracker.merge(
            videos = listOf(video(1, "9")),
            history = baseline,
            now = 200L,
            airedEpisodeCounts = mapOf(1 to 8),
        )

        assertEquals(9, updated.single().episodeNumber)
        assertEquals(200L, updated.single().lastUpdatedAt)
    }

    @Test
    fun `specials and invalid episode labels are ignored`() {
        val result = tracker.merge(
            listOf(video(1, "OVA"), video(2, "SP"), video(3, "7.5"), video(4, "0"), video(5, "8")),
            emptyList(),
            100L,
        )

        assertEquals(listOf(5), result.map { it.animeId })
        assertEquals(8, result.single().episodeNumber)
    }

    @Test
    fun `titles absent from current feed remain in local history`() {
        val baseline = tracker.merge(listOf(video(1, "10"), video(2, "4")), emptyList(), 100L)
        val updated = tracker.merge(listOf(video(1, "11")), baseline, 200L)

        assertEquals(setOf(1, 2), updated.map { it.animeId }.toSet())
    }

    @Test
    fun `legacy zero episode entry becomes baseline without false promotion`() {
        val legacy = HomeNewReleaseEntry(
            animeId = 7,
            title = "Legacy",
            animeUrl = "",
            description = "",
            episodeNumber = 0,
            episodeTitle = "",
            videoId = null,
            dubTitle = null,
            playerTitle = null,
            posterSmallUrl = null,
            posterMediumUrl = null,
            posterBigUrl = null,
            posterFullsizeUrl = null,
            posterMegaUrl = null,
            firstSeenAt = 50L,
            lastDetectedAt = 50L,
            lastUpdatedAt = 0L,
            position = 3,
        )

        val result = tracker.merge(listOf(video(7, "10")), listOf(legacy), 100L).single()

        assertEquals(10, result.episodeNumber)
        assertEquals(50L, result.firstSeenAt)
        assertEquals(0L, result.lastUpdatedAt)
        assertEquals(3, result.position)
    }

    @Test
    fun `empty response never clears history`() {
        val baseline = tracker.merge(listOf(video(1, "10")), emptyList(), 100L)
        assertEquals(baseline, tracker.merge(emptyList(), baseline, 200L))
    }

    @Test
    fun `episode parser accepts whole numbers but rejects fractional specials`() {
        assertEquals(10, " 010 ".toRegularEpisodeNumber())
        assertEquals(10, "10.0".toRegularEpisodeNumber())
        assertEquals(10, "10,00 серия".toRegularEpisodeNumber())
        assertFalse("10.5".toRegularEpisodeNumber() == 10)
        assertEquals(null, "OVA 10".toRegularEpisodeNumber())
    }

    private fun video(
        animeId: Int,
        episode: String,
        dub: String = "Dub",
        videoId: Int = 1,
    ) = YaniVideoDto(
        videoId = videoId,
        animeId = animeId,
        animeUrl = "/anime/$animeId",
        title = "Anime $animeId",
        description = "Description",
        episodeTitle = episode,
        dubTitle = dub,
        playerTitle = "Player",
    )
}
