package su.afk.yummy.tv.feature.schedule.mapper

import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
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
        zone = zone,
        today = today,
        nowSeconds = now.toEpochSecond(),
        prioritizedAnimeIds = prioritizedAnimeIds,
        favoriteAnimeIds = favoriteAnimeIds,
        libraryOnly = libraryOnly,
        libraryAnimeIds = libraryAnimeIds,
        hideWatched = hideWatched,
        watchedEpisodes = watchedEpisodes,
    )
    val availableEpochDays = dayGroups.map { it.date.toEpochDay() }
    val nextSelectedEpochDay = selectedEpochDay(
        availableEpochDays = availableEpochDays,
        todayEpochDay = today.toEpochDay(),
        selectedEpochDay = selectedEpochDay,
    )

    return ScheduleTimelineUi(
        zone = zone,
        now = now,
        dayGroups = dayGroups,
        selectedEpochDay = nextSelectedEpochDay,
    )
}

internal fun ScheduleTimelineUi.withSelectedDay(epochDay: Long): ScheduleTimelineUi =
    copy(selectedEpochDay = epochDay.takeIf { it in availableEpochDays } ?: selectedEpochDay)

private fun selectedEpochDay(
    availableEpochDays: List<Long>,
    todayEpochDay: Long,
    selectedEpochDay: Long?,
): Long? =
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
    flatMap { it.items }
        .flatMap { item ->
            buildList {
                // Feed may include a scheduled video with a future timestamp. It is not an aired episode;
                // future entries must come from the schedule API's nextDate instead.
                val airedCount = item.airedEpisodes?.takeIf { it > 0 }
                // airedEpisodes includes the currently released episode. Reject only older
                // uploads; a local page cache can already contain a genuinely new episode.
                val latestEligibleRelease = item.videoReleases
                    .asSequence()
                    .filter { release ->
                        airedCount != null && release.episodeNumber >= airedCount
                    }
                    .filter { it.firstVideoAtSeconds in 1..nowSeconds }
                    .sortedWith(
                        compareByDescending<su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease> { it.episodeNumber }
                            .thenBy { it.firstVideoAtSeconds },
                    )
                    .firstOrNull()
                latestEligibleRelease?.let { release ->
                    add(
                        ScheduleReleaseUi(
                            item = item,
                            epochSeconds = release.firstVideoAtSeconds,
                            episode = release.episodeNumber,
                            aired = true,
                            isFromSelectedList = item.animeId in prioritizedAnimeIds,
                            isFavorite = item.animeId in favoriteAnimeIds,
                        ),
                    )
                }
                // Японское расписание используется только для будущих серий.
                item.nextDateEpochSeconds
                    ?.takeIf { it > nowSeconds }
                    ?.let { nextDate ->
                        add(
                            ScheduleReleaseUi(
                                item = item,
                                epochSeconds = nextDate,
                                episode = (item.airedEpisodes ?: 0) + 1,
                                aired = false,
                                isFromSelectedList = item.animeId in prioritizedAnimeIds,
                                isFavorite = item.animeId in favoriteAnimeIds,
                            )
                        )
                    }
            }
        }
        .filter { release ->
            !hideWatched || release.episode !in watchedEpisodes[release.item.animeId].orEmpty()
        }
        .filter { release ->
            !libraryOnly || release.item.animeId in libraryAnimeIds || release.isFromSelectedList || release.isFavorite
        }
        .filter { release ->
            Instant.ofEpochSecond(release.epochSeconds).atZone(zone).toLocalDate() >= today.minusDays(1)
        }
        // Resolve duplicate upstream rows deterministically: keep the earliest publication
        // timestamp, then deduplicate by the exact key consumed by both mobile and TV LazyColumns.
        .sortedBy { it.epochSeconds }
        .distinctBy { it.focusKey }
        .groupBy { release ->
            Instant.ofEpochSecond(release.epochSeconds).atZone(zone).toLocalDate()
        }
        .toSortedMap()
        .map { (date, items) ->
            // Календарь показывает один пункт на тайтл в конкретный день, а не отдельную
            // карточку на каждую озвучку/переиздание/уже вышедшую серию.
            // Если на тот же день назначена будущая серия, показываем её; иначе — самую
            // новую опубликованную серию. Это также делает счётчик дня числом тайтлов.
            val dailyTitles = items
                .groupBy { it.item.animeId }
                .values
                .mapNotNull { titleReleases ->
                    titleReleases.maxWithOrNull(
                        compareBy<ScheduleReleaseUi> { !it.aired }
                            .thenBy { it.episode }
                            .thenBy { it.epochSeconds },
                    )
                }
                // Keep the calendar chronological; library membership must never reorder releases.
                .sortedBy { it.epochSeconds }
            ScheduleDayUi(
                date = date,
                weekdayLabel = date.dayOfWeek
                    .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
                    .trimEnd('.')
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                items = dailyTitles,
                isToday = date == today,
            )
        }
