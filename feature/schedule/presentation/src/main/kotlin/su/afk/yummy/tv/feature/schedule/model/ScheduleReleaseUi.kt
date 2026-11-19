package su.afk.yummy.tv.feature.schedule.model

import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleItem
import su.afk.yummy.tv.domain.schedule.model.ReleasePrediction

data class ScheduleReleaseUi(
    val item: AnimeScheduleItem,
    val epochSeconds: Long,
    val episode: Int,
    val aired: Boolean,
    val isFromSelectedList: Boolean = false,
    val isFavorite: Boolean = false,
    val isPredicted: Boolean = false,
    val predictionConfidence: ReleasePrediction.Confidence? = null,
) {
    val focusKey: String = "${item.animeId}:$episode:$aired"
}
