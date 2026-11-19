package su.afk.yummy.tv.domain.schedule.model

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Robust, source-isolated weekly publication estimator.
 *
 * Intervals are calculated only between consecutive episodes from the same player+dubbing pair.
 * A weighted median limits the influence of exceptional delays; recent observations receive
 * exponentially greater weight. No date is fabricated when fewer than two intervals exist.
 */
object ReleasePredictionCalculator {
    private const val HALF_LIFE_DAYS = 42.0
    private const val MIN_INTERVAL_DAYS = 4.0
    private const val MAX_INTERVAL_DAYS = 35.0

    fun predict(
        releases: List<AnimeEpisodeRelease>,
        nowSeconds: Long,
        preferredSource: Pair<String, String>? = null,
    ): ReleasePrediction? {
        val groups = releases
            .filter { it.firstVideoAtSeconds > 0L }
            .groupBy { it.playerName.trim().lowercase() to it.dubbing.trim().lowercase() }
            .filterKeys { (player, dub) -> player.isNotBlank() || dub.isNotBlank() }
        val usable = groups.values
            .mapNotNull { source -> source.distinctBy { it.episodeNumber }
                .sortedBy { it.episodeNumber }.takeIf { it.size >= 3 } }
        val preferred = preferredSource?.let { (player, dubbing) ->
            usable.firstOrNull { source ->
                source.last().playerName.equals(player, ignoreCase = true) &&
                    source.last().dubbing.equals(dubbing, ignoreCase = true)
            }
        }
        val selected = preferred ?: usable.maxWithOrNull(
            compareBy<List<AnimeEpisodeRelease>> { it.size }
                .thenBy { it.last().firstVideoAtSeconds },
        ) ?: return null

        val intervals = selected.zipWithNext().mapNotNull { (a, b) ->
            val days = (b.firstVideoAtSeconds - a.firstVideoAtSeconds) / 86400.0
            days.takeIf { it in MIN_INTERVAL_DAYS..MAX_INTERVAL_DAYS }
                ?.let { Interval(it, b.firstVideoAtSeconds) }
        }
        if (intervals.isEmpty()) return null

        val newest = selected.last()
        val weightedInterval = weightedMedian(intervals, nowSeconds)
        val expected = newest.firstVideoAtSeconds + (weightedInterval * 86400.0).toLong()
        if (expected <= nowSeconds - 86400L) return null

        val recent = intervals.takeLast(6).map { it.days }
        val median = median(recent)
        val mad = median(recent.map { abs(it - median) })
        val stability = when {
            intervals.size >= 6 && mad <= 1.5 -> ReleasePrediction.Confidence.HIGH
            intervals.size >= 3 && mad <= 3.5 -> ReleasePrediction.Confidence.MEDIUM
            else -> ReleasePrediction.Confidence.LOW
        }
        val ageDays = max(0.0, (nowSeconds - newest.firstVideoAtSeconds) / 86400.0)
        val confidence = if (ageDays > 45.0) ReleasePrediction.Confidence.UNKNOWN else
            if (ageDays > 21.0 && stability == ReleasePrediction.Confidence.HIGH)
                ReleasePrediction.Confidence.MEDIUM else stability

        val spreadSeconds = max(3600L, (mad * 86400.0).toLong())
        return ReleasePrediction(
            expectedAtEpochSeconds = expected,
            windowStartEpochSeconds = expected - spreadSeconds,
            windowEndEpochSeconds = expected + spreadSeconds,
            confidence = confidence,
            sourcePlayer = newest.playerName,
            sourceDubbing = newest.dubbing,
            observations = selected.size,
        )
    }

    private data class Interval(val days: Double, val observedAt: Long)

    private fun weightedMedian(values: List<Interval>, now: Long): Double {
        val sorted = values.sortedBy { it.days }
        val weights = sorted.map {
            exp(-kotlin.math.ln(2.0) * max(0.0, (now - it.observedAt) / 86400.0) / HALF_LIFE_DAYS)
        }
        val half = weights.sum() / 2.0
        var cumulative = 0.0
        for (index in sorted.indices) {
            cumulative += weights[index]
            if (cumulative >= half) return sorted[index].days
        }
        return sorted.last().days
    }

    private fun median(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[middle - 1] + sorted[middle]) / 2.0 else sorted[middle]
    }
}
