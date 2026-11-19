package su.afk.yummy.tv.domain.schedule.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReleasePredictionCalculatorTest {
    @Test fun sourceHistoriesAreNotMixed() {
        val releases = (1..5).flatMap { episode ->
            listOf(
                release(episode, 1_700_000_000L + episode * 7L * 86400L, "NDA", "NDA Studio"),
                release(episode, 1_700_000_000L + episode * 7L * 86400L + 2L * 86400L, "AniLib", "AniLibria"),
            )
        }
        val result = ReleasePredictionCalculator.predict(releases, 1_740_000_000L)
        assertNotNull(result)
        assertEquals(5, result.observations)
        assertEquals("NDA Studio", result.sourceDubbing)
    }

    @Test fun insufficientHistoryDoesNotInventPrediction() {
        assertNull(ReleasePredictionCalculator.predict(listOf(
            release(1, 1_700_000_000L, "A", "Dub"),
            release(2, 1_700_604_800L, "A", "Dub"),
        ), 1_701_000_000L))
    }

    private fun release(number: Int, at: Long, player: String, dubbing: String) =
        AnimeEpisodeRelease(1, number, at, number, "Anime", playerName = player, dubbing = dubbing)
}
