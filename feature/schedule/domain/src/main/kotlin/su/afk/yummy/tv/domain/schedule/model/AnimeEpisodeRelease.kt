package su.afk.yummy.tv.domain.schedule.model

/**
 * First observed publication of a concrete video in YummyAnime's feed.
 * Feed publication time and local first-detection time are kept separately.
 */
data class AnimeEpisodeRelease(
    val animeId: Int,
    val episodeNumber: Int,
    val firstVideoAtSeconds: Long,
    val firstVideoId: Int,
    val title: String,
    val posterUrl: String? = null,
    val playerName: String = "",
    val dubbing: String = "",
    val firstDetectedAtSeconds: Long = firstVideoAtSeconds,
    val animeUrl: String? = null,
    val description: String? = null,
    val episodeTitle: String? = null,
    val posterSmallUrl: String? = null,
    val posterMediumUrl: String? = null,
    val posterBigUrl: String? = null,
    val posterFullsizeUrl: String? = null,
    val posterMegaUrl: String? = null,
)
