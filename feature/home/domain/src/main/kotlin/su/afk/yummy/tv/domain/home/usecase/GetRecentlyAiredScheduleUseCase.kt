package su.afk.yummy.tv.domain.home.usecase

import su.afk.yummy.tv.domain.home.model.HomeFeedItem
import su.afk.yummy.tv.domain.home.model.HomeFeedItemAction
import su.afk.yummy.tv.domain.schedule.usecase.GetAnimeScheduleUseCase
import javax.inject.Inject

/**
 * Возвращает тайтлы, для серий которых на YummyAnime действительно появилось видео за последние
 * 14 дней. Время публикации видео используется только для серии, не старше известного количества
 * доступных/вышедших эпизодов. Старые серии, поздние озвучки и переиздания отсекаются.
 */
class GetRecentlyAiredScheduleUseCase @Inject constructor(
    private val getSchedule: GetAnimeScheduleUseCase,
) {
    suspend operator fun invoke(nowSeconds: Long): List<HomeFeedItem> {
        val threshold = nowSeconds - RECENT_WINDOW_SECONDS
        return getSchedule()
            .flatMap { it.items }
            .flatMap { item ->
                val availableCount = item.availableEpisodeCount?.takeIf { it > 0 }
                val airedCount = item.airedEpisodes?.takeIf { it > 0 }
                if (availableCount == null && airedCount == null) return@flatMap emptyList()
                // If the anime page already knows an episode, another upload of it is not new.
                // Without a local video count, use the schedule's current aired episode as fallback.
                val release = item.videoReleases
                    .asSequence()
                    .filter { release ->
                        if (availableCount != null) release.episodeNumber > availableCount
                        else release.episodeNumber >= airedCount!!
                    }
                    .filter { it.firstVideoAtSeconds in 1..nowSeconds }
                    .sortedWith(
                        compareByDescending<su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease> { it.episodeNumber }
                            .thenBy { it.firstVideoAtSeconds },
                    )
                    .firstOrNull()
                    ?: return@flatMap emptyList()
                listOf(
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
                    ),
                )
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
