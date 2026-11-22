package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplayMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.feature.player.presentation.R

@Composable
internal fun MobilePlayerTimeDisplayPage(
    settings: PlayerTimeDisplaySettings,
    onSettingsChange: (PlayerTimeDisplaySettings) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(settings.elapsed, settings.duration, settings.separateTimecodes) {
        val elapsed = settings.elapsed.takeUnless { it == PlayerTimeDisplayMode.NEVER } ?: PlayerTimeDisplayMode.CONTROLS
        val duration = settings.duration.takeUnless { it == PlayerTimeDisplayMode.NEVER } ?: PlayerTimeDisplayMode.CONTROLS
        val sharedDuration = if (settings.separateTimecodes) duration else elapsed
        if (elapsed != settings.elapsed || sharedDuration != settings.duration) {
            onSettingsChange(settings.copy(elapsed = elapsed, duration = sharedDuration))
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        MobilePlayerMenuPageHeader(
            title = stringResource(R.string.player_time_display_title),
            onBack = onBack,
        )
        TimeDisplayModeRow(
            label = stringResource(R.string.player_time_display_system_clock),
            mode = settings.systemClock,
            onSelected = { onSettingsChange(settings.copy(systemClock = it)) },
        )
        MobilePlayerMenuRow(
            label = stringResource(R.string.player_time_display_combine_clock_finish),
            trailing = MobilePlayerMenuRowTrailing.Toggle(settings.combineClockAndFinishAt),
            onClick = {
                onSettingsChange(settings.copy(combineClockAndFinishAt = !settings.combineClockAndFinishAt))
            },
        )
        MobilePlayerMenuRow(
            label = stringResource(R.string.player_time_display_separate),
            trailing = MobilePlayerMenuRowTrailing.Toggle(settings.separateTimecodes),
            onClick = {
                onSettingsChange(settings.copy(separateTimecodes = !settings.separateTimecodes, duration = settings.elapsed))
            },
        )
        if (settings.separateTimecodes) {
            TimeDisplayModeRow(
                label = stringResource(R.string.player_time_display_elapsed),
                mode = settings.elapsed,
                allowNever = false,
                onSelected = { onSettingsChange(settings.copy(elapsed = it)) },
            )
            TimeDisplayModeRow(
                label = stringResource(R.string.player_time_display_duration),
                mode = settings.duration,
                allowNever = false,
                onSelected = { onSettingsChange(settings.copy(duration = it)) },
            )
        } else {
            TimeDisplayModeRow(
                label = stringResource(R.string.player_time_display_combined),
                mode = settings.elapsed,
                allowNever = false,
                onSelected = { onSettingsChange(settings.copy(elapsed = it, duration = it)) },
            )
        }
        TimeDisplayModeRow(
            label = stringResource(R.string.player_time_display_finish_at),
            mode = settings.finishAt,
            onSelected = { onSettingsChange(settings.copy(finishAt = it)) },
        )
    }
}

@Composable
private fun TimeDisplayModeRow(
    label: String,
    mode: PlayerTimeDisplayMode,
    onSelected: (PlayerTimeDisplayMode) -> Unit,
    allowNever: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        MobilePlayerMenuRow(
            label = label,
            trailing = MobilePlayerMenuRowTrailing.Navigate(
                when (mode) {
                    PlayerTimeDisplayMode.ALWAYS -> stringResource(R.string.player_time_display_always)
                    PlayerTimeDisplayMode.CONTROLS -> stringResource(R.string.player_time_display_controls)
                    PlayerTimeDisplayMode.NEVER -> if (allowNever) stringResource(R.string.player_time_display_never) else stringResource(R.string.player_time_display_controls)
                },
            ),
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.player_time_display_always)) },
                onClick = { onSelected(PlayerTimeDisplayMode.ALWAYS); expanded = false },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.player_time_display_controls)) },
                onClick = { onSelected(PlayerTimeDisplayMode.CONTROLS); expanded = false },
            )
            if (allowNever) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.player_time_display_never)) },
                    onClick = { onSelected(PlayerTimeDisplayMode.NEVER); expanded = false },
                )
            }
        }
    }
}
