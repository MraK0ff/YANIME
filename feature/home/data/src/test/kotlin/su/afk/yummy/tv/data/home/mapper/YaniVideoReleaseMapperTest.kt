package su.afk.yummy.tv.data.home.mapper

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import su.afk.yummy.tv.data.home.dto.YaniFeedDto
import su.afk.yummy.tv.core.testing.BaseUnitTest

class YaniVideoReleaseMapperTest : BaseUnitTest() {

    @Test
    fun `numeric publication dates are read from each feed video`() {
        val dto = Json.decodeFromString<YaniFeedDto>(
            """
            {
              "response": {
                "new_videos": [
                  {"video_id": 10, "anime_id": 7, "title": "Anime", "ep_title": "Episode 4", "date": 1790000100},
                  {"video_id": 11, "anime_id": 7, "title": "Anime", "ep_title": "4 серия", "date": 1790000200}
                ]
              }
            }
            """.trimIndent(),
        )

        val releases = dto.response.newVideos.toAnimeEpisodeReleases()

        assertEquals(2, releases.size)
        assertEquals(4, releases.first().episodeNumber)
        assertEquals(1_790_000_100L, releases.first().firstVideoAtSeconds)
        assertEquals(10, releases.first().firstVideoId)
    }

    @Test
    fun `ISO publication date is converted to epoch seconds`() {
        val dto = Json.decodeFromString<YaniFeedDto>(
            """
            {
              "response": {
                "new_videos": [
                  {"video_id": 20, "anime_id": 8, "title": "Anime", "ep_title": "12 серия", "date": "2026-11-14T18:30:00Z"}
                ]
              }
            }
            """.trimIndent(),
        )

        val release = dto.response.newVideos.toAnimeEpisodeReleases().single()

        assertEquals(12, release.episodeNumber)
        assertEquals(Instant.parse("2026-11-14T18:30:00Z").epochSecond, release.firstVideoAtSeconds)
    }

    @Test
    fun `special episode numbers are not treated as regular episodes`() {
        val dto = Json.decodeFromString<YaniFeedDto>(
            """
            {
              "response": {
                "new_videos": [
                  {"video_id": 30, "anime_id": 9, "title": "Anime", "ep_title": "4.5", "date": 1790000100}
                ]
              }
            }
            """.trimIndent(),
        )

        assertTrue(dto.response.newVideos.toAnimeEpisodeReleases().isEmpty())
    }

}
