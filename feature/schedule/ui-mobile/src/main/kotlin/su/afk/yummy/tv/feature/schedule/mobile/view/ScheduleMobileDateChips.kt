package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.feature.schedule.model.ScheduleDayUi

@Composable
internal fun ScheduleMobileDateChips(
    groups: List<ScheduleDayUi>,
    selectedEpochDay: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chipSpacing = 4.dp
        val chipWidth = ((maxWidth - chipSpacing * 6) / 7).coerceAtLeast(1.dp)
        val selectedIndex = groups.indexOfFirst { it.date.toEpochDay() == selectedEpochDay }
        val itemWidthPx = with(density) { chipWidth.roundToPx() }

        LaunchedEffect(selectedEpochDay, groups.size, chipWidth) {
            if (selectedIndex >= 0) {
                val viewport = listState.layoutInfo.viewportSize.width
                val centeredOffset = -((viewport - itemWidthPx) / 2).coerceAtLeast(0)
                listState.animateScrollToItem(selectedIndex, centeredOffset)
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(chipSpacing),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(groups, key = { it.date.toEpochDay() }) { group ->
                val epochDay = group.date.toEpochDay()
                ScheduleMobileDateChip(
                    group = group,
                    selected = epochDay == selectedEpochDay,
                    onSelected = { onSelected(epochDay) },
                    chipWidth = chipWidth,
                )
            }
        }
    }
}
