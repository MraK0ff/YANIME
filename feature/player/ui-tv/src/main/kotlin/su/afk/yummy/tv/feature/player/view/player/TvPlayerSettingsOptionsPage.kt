package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplayMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.model.settings.PlayerZoomLevel
import su.afk.yummy.tv.feature.player.model.TvPlayerSettingsPage
import su.afk.yummy.tv.feature.player.presentation.R
import su.afk.yummy.tv.feature.player.utils.isSelected
import su.afk.yummy.tv.feature.player.utils.tvIcon
import su.afk.yummy.tv.feature.player.utils.tvLabel
import su.afk.yummy.tv.feature.player.utils.tvPlayerScaleOptions

/** Подменю настроек: масштаб или поведение временных меток. */
@Composable
internal fun TvPlayerSettingsOptionsPage(
    page: TvPlayerSettingsPage,
    optionFocus: FocusRequester,
    resizeMode: PlayerResizeMode,
    zoomLevel: PlayerZoomLevel,
    onResizeModeSelected: (PlayerResizeMode) -> Unit,
    onZoomLevelSelected: (PlayerZoomLevel) -> Unit,
    timeDisplaySettings: PlayerTimeDisplaySettings,
    onTimeDisplaySettingsChange: (PlayerTimeDisplaySettings) -> Unit,
    onDone: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(28.dp),
        )
        TvPlayerSettingsTitle(
            when (page) {
                TvPlayerSettingsPage.Scale -> stringResource(R.string.player_resize_title)
                TvPlayerSettingsPage.TimeDisplay -> stringResource(R.string.player_time_display_title)
            },
        )
    }

    when (page) {
        TvPlayerSettingsPage.Scale -> {
            val selectedIndex = tvPlayerScaleOptions.indexOfFirst { it.isSelected(resizeMode, zoomLevel) }
            tvPlayerScaleOptions.forEachIndexed { index, option ->
                PlayerSelectionItem(
                    label = option.tvLabel(),
                    metaContent = {},
                    selected = index == selectedIndex,
                    enabled = true,
                    icon = option.tvIcon(),
                    modifier = if (index == selectedIndex) Modifier.focusRequester(optionFocus) else Modifier,
                    onExitDown = null,
                    onClick = {
                        val level = option.zoomLevel
                        if (level != null) onZoomLevelSelected(level) else onResizeModeSelected(option.mode)
                        onDone()
                    },
                )
            }
        }

        TvPlayerSettingsPage.TimeDisplay -> {
            @Composable
            fun modeLabel(mode: PlayerTimeDisplayMode): String = stringResource(
                when (mode) {
                    PlayerTimeDisplayMode.ALWAYS -> R.string.player_time_display_always
                    PlayerTimeDisplayMode.CONTROLS -> R.string.player_time_display_controls
                    PlayerTimeDisplayMode.NEVER -> R.string.player_time_display_never
                },
            )

            fun nextMode(current: PlayerTimeDisplayMode, direction: Int, allowNever: Boolean): PlayerTimeDisplayMode {
                val values = if (allowNever) PlayerTimeDisplayMode.entries else listOf(
                    PlayerTimeDisplayMode.ALWAYS,
                    PlayerTimeDisplayMode.CONTROLS,
                )
                val index = values.indexOf(current).coerceAtLeast(0)
                return values[(index + direction + values.size) % values.size]
            }

            TvPlayerSettingsHeader(stringResource(R.string.player_settings_section_playback))
            TvPlayerSettingsStepperRow(
                label = stringResource(R.string.player_time_display_system_clock),
                value = modeLabel(timeDisplaySettings.systemClock),
                modifier = Modifier.focusRequester(optionFocus),
                onStep = { direction ->
                    onTimeDisplaySettingsChange(
                        timeDisplaySettings.copy(systemClock = nextMode(timeDisplaySettings.systemClock, direction, true)),
                    )
                },
            )
            if (timeDisplaySettings.separateTimecodes) {
                TvPlayerSettingsStepperRow(
                    label = stringResource(R.string.player_time_display_elapsed),
                    value = modeLabel(timeDisplaySettings.elapsed),
                    onStep = { direction ->
                        onTimeDisplaySettingsChange(
                            timeDisplaySettings.copy(elapsed = nextMode(timeDisplaySettings.elapsed, direction, false)),
                        )
                    },
                )
                TvPlayerSettingsStepperRow(
                    label = stringResource(R.string.player_time_display_duration),
                    value = modeLabel(timeDisplaySettings.duration),
                    onStep = { direction ->
                        onTimeDisplaySettingsChange(
                            timeDisplaySettings.copy(duration = nextMode(timeDisplaySettings.duration, direction, false)),
                        )
                    },
                )
            }
            TvPlayerSettingsStepperRow(
                label = stringResource(R.string.player_time_display_finish_at),
                value = modeLabel(timeDisplaySettings.finishAt),
                onStep = { direction ->
                    onTimeDisplaySettingsChange(
                        timeDisplaySettings.copy(finishAt = nextMode(timeDisplaySettings.finishAt, direction, true)),
                    )
                },
            )
            TvPlayerSettingsMenuRow(
                label = stringResource(R.string.player_time_display_separate),
                value = stringResource(if (timeDisplaySettings.separateTimecodes) R.string.player_toggle_on else R.string.player_toggle_off),
                toggleChecked = timeDisplaySettings.separateTimecodes,
                showChevron = false,
                onClick = {
                    onTimeDisplaySettingsChange(
                        timeDisplaySettings.copy(separateTimecodes = !timeDisplaySettings.separateTimecodes),
                    )
                },
            )
        }
    }
}
