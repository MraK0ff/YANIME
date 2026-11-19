package su.afk.yummy.tv.domain.schedule.model

/** Statistical estimate of the next YummyAnime video publication, not the Japanese broadcast. */
data class ReleasePrediction(
    val expectedAtEpochSeconds: Long?,
    val windowStartEpochSeconds: Long?,
    val windowEndEpochSeconds: Long?,
    val confidence: Confidence,
    val sourcePlayer: String,
    val sourceDubbing: String,
    val observations: Int,
) {
    enum class Confidence { HIGH, MEDIUM, LOW, UNKNOWN }
}
