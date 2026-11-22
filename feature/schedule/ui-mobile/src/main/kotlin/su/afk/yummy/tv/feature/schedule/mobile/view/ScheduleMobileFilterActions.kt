package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.feature.schedule.ScheduleState
import su.afk.yummy.tv.feature.schedule.presentation.R

@Composable
internal fun ScheduleLibraryFilterAction(
    selected: ScheduleState.LibraryFilter,
    onSelected: (ScheduleState.LibraryFilter) -> Unit,
) {
    val inLibrary = selected == ScheduleState.LibraryFilter.LIBRARY
    IconButton(
        onClick = {
            onSelected(
                if (inLibrary) ScheduleState.LibraryFilter.ALL
                else ScheduleState.LibraryFilter.LIBRARY,
            )
        },
    ) {
        Icon(
            imageVector = if (inLibrary) Icons.Filled.VideoLibrary else Icons.Filled.Apps,
            contentDescription = stringResource(
                if (inLibrary) R.string.schedule_filter_in_library else R.string.schedule_filter_all,
            ),
            tint = if (inLibrary) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun ScheduleHideWatchedAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    IconButton(onClick = { onCheckedChange(!checked) }) {
        Icon(
            imageVector = if (checked) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = stringResource(R.string.schedule_hide_watched),
            tint = if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
