package su.afk.yummy.tv.domain.schedule.model

/**
 * Первая публикация реального видео для конкретной серии на YummyAnime.
 * Дополнительные озвучки и версии той же серии не создают новые записи.
 */
data class AnimeEpisodeRelease(
    val animeId: Int,
    val episodeNumber: Int,
    val firstVideoAtSeconds: Long,
    val firstVideoId: Int,
    val title: String,
    val posterUrl: String? = null,
)
