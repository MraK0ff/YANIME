package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.feature.schedule.ScheduleState
import su.afk.yummy.tv.feature.schedule.presentation.R

@Composable
internal fun ScheduleLibraryFilterAction(
    selected: ScheduleState.LibraryFilter,
    onSelected: (ScheduleState.LibraryFilter) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.FilterList,
            contentDescription = stringResource(
                if (selected == ScheduleState.LibraryFilter.ALL) R.string.schedule_filter_all
                else R.string.schedule_filter_library,
            ),
            tint = if (selected == ScheduleState.LibraryFilter.ALL) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.primary,
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        ScheduleState.LibraryFilter.entries.forEach { filter ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(if (filter == ScheduleState.LibraryFilter.ALL) R.string.schedule_filter_all else R.string.schedule_filter_library),
                        color = if (filter == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                },
                onClick = { expanded = false; onSelected(filter) },
            )
        }
    }
}

@Composable
internal fun ScheduleHideWatchedAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    RowForSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

@Composable
private fun RowForSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.material3.TextButton(onClick = { onCheckedChange(!checked) }) {
        Text(stringResource(R.string.schedule_hide_watched))
    }
}
