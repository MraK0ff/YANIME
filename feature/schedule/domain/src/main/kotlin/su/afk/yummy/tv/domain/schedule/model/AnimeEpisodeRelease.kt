package su.afk.yummy.tv.domain.schedule.model

/**
 * First observed publication of a concrete video in YummyAnime's feed.
 * Publication identity includes player and dubbing: alternate versions are independent observations.
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
)
