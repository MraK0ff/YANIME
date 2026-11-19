package su.afk.yummy.tv.data.home.utils

import su.afk.yummy.tv.core.storage.home.HomeNewReleaseEntry
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import java.util.Locale

/**
 * Builds Home's card-ordering projection from the canonical episode publication history.
 *
 * Episode facts (episode number, source, video and first detection) always come from the
 * schedule release store. The Home table only remembers presentation metadata and card order.
 */
internal fun projectHomeNewReleases(
    releases: List<AnimeEpisodeRelease>,
    previousProjection: List<HomeNewReleaseEntry>,
    now: Long,
    airedEpisodeCounts: Map<Int, Int>? = null,
): List<HomeNewReleaseEntry> {
    val previousById = previousProjection.associateBy { it.animeId }
    val merged = previousById.toMutableMap()

    releases.groupBy { it.animeId }.forEach { (animeId, animeReleases) ->
        val newestEpisode = animeReleases.maxOfOrNull { it.episodeNumber } ?: return@forEach
        val candidates = animeReleases.filter { it.episodeNumber == newestEpisode }
        val selected = candidates.minWithOrNull(
            compareBy<AnimeEpisodeRelease> { it.firstDetectedAtSeconds }
                .thenBy { it.playerName.trim().lowercase(Locale.ROOT) }
                .thenBy { it.dubbing.trim().lowercase(Locale.ROOT) }
                .thenBy { it.firstVideoId },
        ) ?: return@forEach

        val airedCount = airedEpisodeCounts?.get(animeId)
        // A missing title in a successfully read schedule is unknown, not evidence that it aired.
        if (airedEpisodeCounts != null && airedCount == null) return@forEach
        if (airedCount != null && newestEpisode < airedCount) {
            merged.remove(animeId)
            return@forEach
        }

        val previous = previousById[animeId]
        val isLegacyBaseline = previous?.episodeNumber == 0
        val promoted = previous != null && previous.episodeNumber > 0 &&
            newestEpisode > previous.episodeNumber
        val firstDetectedAtMs = selected.firstDetectedAtSeconds
            .takeIf { it > 0L }
            ?.let { it * 1_000L }
            ?: now

        merged[animeId] = HomeNewReleaseEntry(
            animeId = animeId,
            title = selected.title.takeIf(String::isNotBlank) ?: previous?.title.orEmpty(),
            animeUrl = selected.animeUrl ?: previous?.animeUrl.orEmpty(),
            description = selected.description ?: previous?.description.orEmpty(),
            episodeNumber = newestEpisode,
            episodeTitle = if (previous?.episodeNumber == newestEpisode) {
                selected.episodeTitle ?: previous.episodeTitle
            } else {
                selected.episodeTitle ?: newestEpisode.toString()
            },
            videoId = selected.firstVideoId,
            dubTitle = selected.dubbing.takeIf(String::isNotBlank) ?: previous?.dubTitle,
            playerTitle = selected.playerName.takeIf(String::isNotBlank) ?: previous?.playerTitle,
            posterSmallUrl = selected.posterSmallUrl ?: previous?.posterSmallUrl,
            posterMediumUrl = selected.posterMediumUrl ?: previous?.posterMediumUrl,
            posterBigUrl = selected.posterBigUrl ?: previous?.posterBigUrl,
            posterFullsizeUrl = selected.posterFullsizeUrl ?: previous?.posterFullsizeUrl,
            posterMegaUrl = selected.posterMegaUrl ?: selected.posterUrl ?: previous?.posterMegaUrl,
            firstSeenAt = previous?.firstSeenAt ?: firstDetectedAtMs,
            lastDetectedAt = now,
            // Initial observations and legacy zero-episode rows establish a baseline.
            lastUpdatedAt = when {
                previous == null || isLegacyBaseline -> 0L
                promoted -> now
                else -> previous.lastUpdatedAt
            },
            position = previous?.position ?: merged.size,
        )
    }

    return merged.values
        .filter { entry ->
            val airedCount = airedEpisodeCounts?.get(entry.animeId)
            airedCount == null || entry.episodeNumber >= airedCount
        }
        .sortedWith(
            compareByDescending<HomeNewReleaseEntry> { it.lastUpdatedAt > 0L }
                .thenByDescending { it.lastUpdatedAt }
                .thenBy { it.position }
                .thenBy { it.animeId },
        )
        .mapIndexed { index, entry -> entry.copy(position = index) }
}
