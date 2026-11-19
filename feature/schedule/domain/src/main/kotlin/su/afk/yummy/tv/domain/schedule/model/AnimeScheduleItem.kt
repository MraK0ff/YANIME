package su.afk.yummy.tv.domain.schedule.model

import su.afk.yummy.tv.core.model.anime.AnimePoster

data class AnimeScheduleItem(
    val animeId: Int,
    val title: String,
    val poster: AnimePoster?,
    val nextDateEpochSeconds: Long?,
    val airedEpisodes: Int?,
    val totalEpisodes: Int?,
    val previousDateEpochSeconds: Long? = null,
    /** Максимальный номер серии из уже загруженного локального списка видео тайтла. */
    val availableEpisodeCount: Int? = null,
    /** Наблюдения публикаций видео из ленты; timestamp не гарантирует премьеру самого эпизода. */
    val videoReleases: List<AnimeEpisodeRelease> = emptyList(),
)
