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
): ScheduleTimelineUi {
    val today = now.toLocalDate()
    val dayGroups = toUiDayGroups(zone = zone, today = today, nowSeconds = now.toEpochSecond())
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
): List<ScheduleDayUi> =
    flatMap { it.items }
        .flatMap { item ->
            buildList {
                // Прошедшая серия появляется в календаре только после публикации видео.
                item.videoReleases.forEach { release ->
                    add(
                        ScheduleReleaseUi(
                            item = item,
                            epochSeconds = release.firstVideoAtSeconds,
                            episode = release.episodeNumber,
                            aired = true,
                        )
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
                            )
                        )
                    }
            }
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
            ScheduleDayUi(
                date = date,
                weekdayLabel = date.dayOfWeek
                    .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
                    .trimEnd('.')
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                items = items.sortedBy { it.epochSeconds },
                isToday = date == today,
            )
        }
