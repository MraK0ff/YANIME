package su.afk.yummy.tv.feature.schedule.mapper

import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
import su.afk.yummy.tv.domain.schedule.model.ReleasePrediction
import su.afk.yummy.tv.feature.schedule.model.ScheduleDayUi
import su.afk.yummy.tv.feature.schedule.model.ScheduleReleaseUi
import su.afk.yummy.tv.feature.schedule.model.ScheduleTimelineUi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

internal fun List<AnimeScheduleDay>.toTimelineUi(
    zone: ZoneId = ZoneId.systemDefault(),
    now: ZonedDateTime = ZonedDateTime.now(zone),
    selectedEpochDay: Long? = null,
    prioritizedAnimeIds: Set<Int> = emptySet(),
    favoriteAnimeIds: Set<Int> = emptySet(),
    libraryOnly: Boolean = false,
    libraryAnimeIds: Set<Int> = emptySet(),
    hideWatched: Boolean = false,
    watchedEpisodes: Map<Int, Set<Int>> = emptyMap(),
): ScheduleTimelineUi {
    val today = now.toLocalDate()
    val dayGroups = toUiDayGroups(
        zone, today, now.toEpochSecond(), prioritizedAnimeIds, favoriteAnimeIds,
        libraryOnly, libraryAnimeIds, hideWatched, watchedEpisodes,
    )
    val availableEpochDays = dayGroups.map { it.date.toEpochDay() }
    val nextSelectedEpochDay = selectedEpochDay(
        availableEpochDays, today.toEpochDay(), selectedEpochDay,
    )
    return ScheduleTimelineUi(zone = zone, now = now, dayGroups = dayGroups, selectedEpochDay = nextSelectedEpochDay)
}

internal fun ScheduleTimelineUi.withSelectedDay(epochDay: Long): ScheduleTimelineUi =
    copy(selectedEpochDay = epochDay.takeIf { it in availableEpochDays } ?: selectedEpochDay)

private fun selectedEpochDay(availableEpochDays: List<Long>, todayEpochDay: Long, selectedEpochDay: Long?): Long? =
    selectedEpochDay?.takeIf { it in availableEpochDays }
        ?: availableEpochDays.firstOrNull { it == todayEpochDay }
        ?: availableEpochDays.firstOrNull { it > todayEpochDay }
        ?: availableEpochDays.lastOrNull()

private fun List<AnimeScheduleDay>.toUiDayGroups(
    zone: ZoneId,
    today: LocalDate,
    nowSeconds: Long,
    prioritizedAnimeIds: Set<Int>,
    favoriteAnimeIds: Set<Int>,
    libraryOnly: Boolean,
    libraryAnimeIds: Set<Int>,
    hideWatched: Boolean,
    watchedEpisodes: Map<Int, Set<Int>>,
): List<ScheduleDayUi> =
    flatMap { it.items }.flatMap { item ->
        buildList {
            val airedCount = item.airedEpisodes?.takeIf { it > 0 }
            val latestEligibleRelease = item.videoReleases.asSequence()
                .filter { airedCount != null && it.episodeNumber >= airedCount }
                .filter { it.firstVideoAtSeconds in 1..nowSeconds }
                .sortedWith(compareByDescending<su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease> { it.episodeNumber }
                    .thenBy { it.firstVideoAtSeconds })
                .firstOrNull()
            latestEligibleRelease?.let { release ->
                add(ScheduleReleaseUi(
                    item = item, epochSeconds = release.firstVideoAtSeconds, episode = release.episodeNumber,
                    aired = true, isFromSelectedList = item.animeId in prioritizedAnimeIds,
                    isFavorite = item.animeId in favoriteAnimeIds,
                ))
            }

            // A Japanese broadcast is not proof that a YummyAnime video exists.
            // Keep the aired episode visible and advance its availability estimate every 10 minutes.
            val currentAiredEpisode = airedCount
            val japaneseReleaseAt = item.previousDateEpochSeconds
            val hasCurrentEpisodeVideo = currentAiredEpisode != null && item.videoReleases.any {
                it.episodeNumber == currentAiredEpisode && it.firstVideoAtSeconds in 1..nowSeconds
            }
            val awaitingVideoAt = if (
                currentAiredEpisode != null &&
                japaneseReleaseAt != null &&
                japaneseReleaseAt <= nowSeconds &&
                Instant.ofEpochSecond(japaneseReleaseAt).atZone(zone).toLocalDate() >= today.minusDays(1) &&
                !hasCurrentEpisodeVideo
            ) {
                nextVideoCheckAt(japaneseReleaseAt, nowSeconds)
            } else {
                null
            }
            awaitingVideoAt?.let { timestamp ->
                add(ScheduleReleaseUi(
                    item = item,
                    epochSeconds = timestamp,
                    episode = currentAiredEpisode!!,
                    aired = false,
                    isFromSelectedList = item.animeId in prioritizedAnimeIds,
                    isFavorite = item.animeId in favoriteAnimeIds,
                    isAwaitingVideo = true,
                ))
            }

            val prediction = item.releasePrediction?.takeIf { value ->
                val expectedAt = value.expectedAtEpochSeconds
                value.confidence != ReleasePrediction.Confidence.UNKNOWN &&
                    expectedAt != null && expectedAt > nowSeconds
            }
            val nextAt = prediction?.expectedAtEpochSeconds
                ?: item.nextDateEpochSeconds?.takeIf { it > nowSeconds }
            nextAt?.let { timestamp ->
                add(ScheduleReleaseUi(
                    item = item,
                    epochSeconds = timestamp,
                    episode = (item.airedEpisodes ?: 0) + 1,
                    aired = false,
                    isFromSelectedList = item.animeId in prioritizedAnimeIds,
                    isFavorite = item.animeId in favoriteAnimeIds,
                    isPredicted = prediction != null,
                    predictionConfidence = prediction?.confidence,
                ))
            }
        }
    }
        .filter { !hideWatched || it.episode !in watchedEpisodes[it.item.animeId].orEmpty() }
        .filter { !libraryOnly || it.item.animeId in libraryAnimeIds || it.isFromSelectedList || it.isFavorite }
        .filter { Instant.ofEpochSecond(it.epochSeconds).atZone(zone).toLocalDate() >= today.minusDays(1) }
        .sortedBy { it.epochSeconds }
        .distinctBy { it.focusKey }
        .groupBy { Instant.ofEpochSecond(it.epochSeconds).atZone(zone).toLocalDate() }
        .toSortedMap()
        .map { (date, items) ->
            val dailyTitles = items.groupBy { it.item.animeId }.values
                .mapNotNull { releases ->
                    releases.maxWithOrNull(compareBy<ScheduleReleaseUi> { !it.aired }
                        .thenBy { it.episode }.thenBy { it.epochSeconds })
                }
                .sortedBy { it.epochSeconds }
            ScheduleDayUi(
                date = date,
                weekdayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
                    .trimEnd('.').replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                items = dailyTitles,
                isToday = date == today,
            )
        }


private fun nextVideoCheckAt(japaneseReleaseAt: Long, nowSeconds: Long): Long {
    val initialEstimate = japaneseReleaseAt + VIDEO_AVAILABILITY_GRACE_SECONDS
    if (nowSeconds < initialEstimate) return initialEstimate
    val elapsedAfterEstimate = nowSeconds - initialEstimate
    val elapsedIntervals = elapsedAfterEstimate / VIDEO_CHECK_INTERVAL_SECONDS + 1L
    return initialEstimate + elapsedIntervals * VIDEO_CHECK_INTERVAL_SECONDS
}

private const val VIDEO_AVAILABILITY_GRACE_SECONDS = 60L * 60L
private const val VIDEO_CHECK_INTERVAL_SECONDS = 10L * 60L
