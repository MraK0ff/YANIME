package su.afk.yummy.tv.domain.home.usecase

import su.afk.yummy.tv.domain.home.model.HomeFeedItem
import su.afk.yummy.tv.domain.home.model.HomeFeedItemAction
import su.afk.yummy.tv.domain.schedule.usecase.GetAnimeScheduleUseCase
import javax.inject.Inject

/**
 * Возвращает тайтлы, для серий которых на YummyAnime действительно появилось видео за последние
 * 14 дней. Время публикации видео используется только для текущего эпизода по расписанию:
 * старые серии, поздние озвучки и переиздания не считаются новыми эпизодами.
 */
class GetRecentlyAiredScheduleUseCase @Inject constructor(
    private val getSchedule: GetAnimeScheduleUseCase,
) {
    suspend operator fun invoke(nowSeconds: Long): List<HomeFeedItem> {
        val threshold = nowSeconds - RECENT_WINDOW_SECONDS
        return getSchedule()
            .flatMap { it.items }
            .flatMap { item ->
                val knownEpisodeCount = (item.availableEpisodeCount ?: item.airedEpisodes)
                    ?.takeIf { it > 0 }
                    ?: return@flatMap emptyList()
                // Only the current/latest aired episode (or a newer one) can represent a
                // recent release. A late dub of episode 6 must not revive it when 8 have aired.
                val release = item.videoReleases
                    .asSequence()
                    .filter { it.episodeNumber >= knownEpisodeCount }
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
