package su.afk.yummy.tv.data.home.mapper

import su.afk.yummy.tv.core.common.episode.episodeNumberOrNull
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.core.utils.network.toHttpsUrl
import su.afk.yummy.tv.data.home.dto.YaniVideoDto
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

private fun parseIsoPublicationDate(value: String): Long? =
    runCatching { Instant.parse(value).epochSecond }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant().epochSecond }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value.replace(' ', 'T')).toEpochSecond(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDate.parse(value).atStartOfDay().toEpochSecond(ZoneOffset.UTC) }.getOrNull()

fun List<YaniVideoDto>.toAnimeEpisodeReleases(): List<AnimeEpisodeRelease> =
    mapNotNull { video ->
        val animeId = video.animeId?.takeIf { it > 0 } ?: return@mapNotNull null
        val videoId = video.videoId?.takeIf { it > 0 } ?: return@mapNotNull null
        val episodeNumber = video.episodeTitle
            ?.episodeNumberOrNull()
            ?.takeIf { it > 0.0 && it % 1.0 == 0.0 }
            ?.toInt()
            ?: return@mapNotNull null
        val rawDate = (video.date as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
        val publishedAt = rawDate.toLongOrNull()
            ?.let { if (it > 10_000_000_000L) it / 1_000L else it }
            ?: parseIsoPublicationDate(rawDate)
            ?: return@mapNotNull null
        if (publishedAt <= 0) return@mapNotNull null
        AnimeEpisodeRelease(
            animeId = animeId,
            episodeNumber = episodeNumber,
            firstVideoAtSeconds = publishedAt,
            firstVideoId = videoId,
            title = video.title,
            posterUrl = (video.poster?.mega ?: video.poster?.fullsize ?: video.poster?.big
                ?: video.poster?.medium ?: video.poster?.small)?.toHttpsUrl(),
            playerName = video.playerTitle.orEmpty().trim(),
            dubbing = video.dubTitle.orEmpty().trim(),
        )
    }
