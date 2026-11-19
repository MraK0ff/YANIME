package su.afk.yummy.tv.feature.schedule.model

import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleItem

data class ScheduleReleaseUi(
    val item: AnimeScheduleItem,
    val epochSeconds: Long,
    val episode: Int,
    val aired: Boolean,
    val isFromSelectedList: Boolean = false,
) {
    // Different episodes may share the same publication timestamp.
    // The episode number is part of the mapper's deduplication identity,
    // so it must also be part of the LazyColumn item key.
    val focusKey: String = "${item.animeId}:$episode:$aired"
}
