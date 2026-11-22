package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.feature.schedule.model.ScheduleDayUi

@Composable
internal fun ScheduleMobileDateChips(
    groups: List<ScheduleDayUi>,
    selectedEpochDay: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
        contentPadding = PaddingValues(horizontal = YummySpacing.Large, vertical = YummySpacing.XSmall),
    ) {
        items(groups, key = { it.date.toEpochDay() }) { group ->
            val epochDay = group.date.toEpochDay()
            ScheduleMobileDateChip(
                group = group,
                selected = epochDay == selectedEpochDay,
                onSelected = { onSelected(epochDay) },
            )
        }
    }
}
