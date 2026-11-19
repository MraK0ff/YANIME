package su.afk.yummy.tv.domain.schedule.model

/**
 * Первая наблюдённая публикация видео для серии в ленте YummyAnime.
 * Это время появления записи в ленте, а не гарантированная премьера эпизода:
 * поздняя альтернативная озвучка может появиться значительно позже самой серии.
 */
data class AnimeEpisodeRelease(
    val animeId: Int,
    val episodeNumber: Int,
    val firstVideoAtSeconds: Long,
    val firstVideoId: Int,
    val title: String,
    val posterUrl: String? = null,
)
