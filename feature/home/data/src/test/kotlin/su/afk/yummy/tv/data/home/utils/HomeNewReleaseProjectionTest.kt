package su.afk.yummy.tv.data.home.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import su.afk.yummy.tv.core.storage.home.HomeNewReleaseEntry
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease

class HomeNewReleaseProjectionTest {

    @Test
    fun `initial canonical history creates a baseline without promoting cards`() {
        val result = projectHomeNewReleases(
            releases = listOf(release(1, 10, detectedAt = 100), release(2, 5, detectedAt = 100)),
            previousProjection = emptyList(),
            now = 100_000L,
        )

        assertEquals(listOf(1, 2), result.map { it.animeId })
        assertTrue(result.all { it.lastUpdatedAt == 0L })
    }

    @Test
    fun `newer canonical episode promotes only its anime card`() {
        val baseline = projectHomeNewReleases(
            releases = listOf(release(1, 10, 100), release(2, 5, 100)),
            previousProjection = emptyList(),
            now = 100_000L,
        )

        val updated = projectHomeNewReleases(
            releases = listOf(release(1, 10, 100), release(1, 11, 200), release(2, 5, 100)),
            previousProjection = baseline,
            now = 200_000L,
        )

        assertEquals(listOf(1, 2), updated.map { it.animeId })
        assertEquals(200_000L, updated.first().lastUpdatedAt)
        assertEquals(0L, updated.last().lastUpdatedAt)
    }

    @Test
    fun `alternate dubbing of same episode does not promote card or replace first source`() {
        val baseline = projectHomeNewReleases(
            releases = listOf(release(1, 10, 100, videoId = 11, dubbing = "A")),
            previousProjection = emptyList(),
            now = 100_000L,
        )

        val updated = projectHomeNewReleases(
            releases = listOf(
                release(1, 10, 100, videoId = 11, dubbing = "A"),
                release(1, 10, 200, videoId = 22, dubbing = "B"),
            ),
            previousProjection = baseline,
            now = 200_000L,
        )

        assertEquals(11, updated.single().videoId)
        assertEquals(0L, updated.single().lastUpdatedAt)
    }

    @Test
    fun `legacy zero episode projection is upgraded without false promotion`() {
        val legacy = previousEntry(animeId = 7, episodeNumber = 0, firstSeenAt = 50_000L)

        val result = projectHomeNewReleases(
            releases = listOf(release(7, 10, 100)),
            previousProjection = listOf(legacy),
            now = 100_000L,
        ).single()

        assertEquals(10, result.episodeNumber)
        assertEquals(50_000L, result.firstSeenAt)
        assertEquals(0L, result.lastUpdatedAt)
    }

    @Test
    fun `episodes behind schedule aired count are excluded`() {
        val result = projectHomeNewReleases(
            releases = listOf(release(1, 6, 100), release(2, 8, 100)),
            previousProjection = emptyList(),
            now = 100_000L,
            airedEpisodeCounts = mapOf(1 to 8, 2 to 8),
        )

        assertEquals(listOf(2), result.map { it.animeId })
    }

    private fun release(
        animeId: Int,
        episode: Int,
        detectedAt: Long,
        videoId: Int = episode,
        dubbing: String = "Dub",
    ) = AnimeEpisodeRelease(
        animeId = animeId,
        episodeNumber = episode,
        firstVideoAtSeconds = detectedAt,
        firstVideoId = videoId,
        title = "Anime $animeId",
        posterUrl = "https://example.com/$animeId.jpg",
        playerName = "Player",
        dubbing = dubbing,
        firstDetectedAtSeconds = detectedAt,
    )

    private fun previousEntry(
        animeId: Int,
        episodeNumber: Int,
        firstSeenAt: Long,
    ) = HomeNewReleaseEntry(
        animeId = animeId,
        title = "Anime $animeId",
        animeUrl = "",
        description = "",
        episodeNumber = episodeNumber,
        episodeTitle = episodeNumber.toString(),
        videoId = null,
        dubTitle = null,
        playerTitle = null,
        posterSmallUrl = null,
        posterMediumUrl = null,
        posterBigUrl = null,
        posterFullsizeUrl = null,
        posterMegaUrl = null,
        firstSeenAt = firstSeenAt,
        lastDetectedAt = firstSeenAt,
        lastUpdatedAt = 0L,
        position = 0,
    )
}
