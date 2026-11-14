package su.afk.yummy.tv.data.home.mapper

import su.afk.yummy.tv.core.common.episode.episodeNumberOrNull
import su.afk.yummy.tv.core.model.anime.AnimePoster
import su.afk.yummy.tv.core.model.anime.AnimeEpisodeRelease
import su.afk.yummy.tv.core.utils.network.toHttpsUrl
import su.afk.yummy.tv.data.home.dto.YaniVideoDto

/**
 * /feed/new_videos содержит дату публикации конкретного видео. Серия извлекается из ep_title;
 * строки без однозначного целого номера не превращаются в обычные эпизоды.
 */
fun List<YaniVideoDto>.toAnimeEpisodeReleases(): List<AnimeEpisodeRelease> =
    mapNotNull { video ->
        val animeId = video.animeId?.takeIf { it > 0 } ?: return@mapNotNull null
        val videoId = video.videoId?.takeIf { it > 0 } ?: return@mapNotNull null
        val episodeNumber = video.episodeTitle
            ?.episodeNumberOrNull()
            ?.takeIf { it > 0.0 && it % 1.0 == 0.0 }
            ?.toInt()
            ?: return@mapNotNull null
        val publishedAt = video.date
            ?.let { if (it > 10_000_000_000L) it / 1_000L else it }
            ?.takeIf { it > 0 }
            ?: return@mapNotNull null
        AnimeEpisodeRelease(
            animeId = animeId,
            episodeNumber = episodeNumber,
            firstVideoAtSeconds = publishedAt,
            firstVideoId = videoId,
            title = video.title,
            posterUrl = (
                video.poster?.mega ?: video.poster?.fullsize ?: video.poster?.big
                    ?: video.poster?.medium ?: video.poster?.small
                )?.toHttpsUrl(),
        )
    }
