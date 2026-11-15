package su.afk.yummy.tv.domain.home.usecase

import su.afk.yummy.tv.domain.home.model.HomeFeedItem
import su.afk.yummy.tv.domain.home.model.HomeFeedItemAction
import su.afk.yummy.tv.domain.schedule.usecase.GetAnimeScheduleUseCase
import javax.inject.Inject

/**
 * Возвращает тайтлы, для серий которых на YummyAnime действительно появилось видео за последние
 * 14 дней. Источник времени — первая публикация видео, а не японское расписание.
 * Повторные озвучки схлопываются в одну запись серии на уровне истории публикаций.
 */
class GetRecentlyAiredScheduleUseCase @Inject constructor(
    private val getSchedule: GetAnimeScheduleUseCase,
) {
    suspend operator fun invoke(nowSeconds: Long): List<HomeFeedItem> {
        val threshold = nowSeconds - RECENT_WINDOW_SECONDS
        return getSchedule()
            .flatMap { it.items }
            .flatMap { item ->
                item.videoReleases.map { release ->
                    release.firstVideoAtSeconds to HomeFeedItem(
                        id = item.animeId,
                        title = item.title.ifBlank { release.title },
                        description = "",
                        poster = item.poster,
                        rating = null,
                        year = null,
                        action = HomeFeedItemAction.OpenSeries(item.animeId),
                        episodeNumber = release.episodeNumber,
                        airedAtSeconds = release.firstVideoAtSeconds,
                    )
                }
            }
            .filter { (publishedAt, _) -> publishedAt in threshold..nowSeconds }
            .sortedByDescending { (publishedAt, _) -> publishedAt }
            .distinctBy { (_, item) -> item.id }
            .map { it.second }
    }

    private companion object {
        const val RECENT_WINDOW_SECONDS = 14L * 24 * 60 * 60
    }
}
