package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.model.settings.PlayerZoomLevel
import su.afk.yummy.tv.feature.player.common.PlayerVolumeController
import su.afk.yummy.tv.feature.player.model.TvPlayerSettingsPage
import su.afk.yummy.tv.feature.player.presentation.R
import su.afk.yummy.tv.feature.player.utils.isSelected
import su.afk.yummy.tv.feature.player.utils.tvLabel
import su.afk.yummy.tv.feature.player.utils.tvPlayerScaleOptions

/** Корень настроек ТВ-плеера: масштаб, отображение времени и звук. */
@Composable
internal fun TvPlayerSettingsRoot(
    entryFocusRequester: FocusRequester,
    rowFocus: Map<TvPlayerSettingsPage, FocusRequester>,
    resizeMode: PlayerResizeMode,
    zoomLevel: PlayerZoomLevel,
    timeDisplaySettings: PlayerTimeDisplaySettings,
    showVolume: Boolean,
    volumePercent: Int,
    onVolumeChange: (Int) -> Unit,
    showStabilization: Boolean,
    stabilizationEnabled: Boolean,
    onStabilizationChange: (Boolean) -> Unit,
    onOpenPage: (TvPlayerSettingsPage) -> Unit,
) {
    @Composable
    fun PageRow(
        page: TvPlayerSettingsPage,
        label: String,
        value: String,
        modifier: Modifier = Modifier,
    ) {
        TvPlayerSettingsMenuRow(
            label = label,
            value = value,
            onClick = { onOpenPage(page) },
            modifier = modifier.focusRequester(rowFocus.getValue(page)),
        )
    }

    TvPlayerSettingsTitle(stringResource(R.string.player_side_panel_title))

    TvPlayerSettingsHeader(stringResource(R.string.player_settings_section_playback))
    PageRow(
        page = TvPlayerSettingsPage.Scale,
        label = stringResource(R.string.player_resize_title),
        value = tvPlayerScaleOptions.firstOrNull { it.isSelected(resizeMode, zoomLevel) }?.tvLabel().orEmpty(),
        modifier = Modifier.focusRequester(entryFocusRequester),
    )
    PageRow(
        page = TvPlayerSettingsPage.TimeDisplay,
        label = stringResource(R.string.player_time_display_title),
        value = "",
    )

    if (showVolume || showStabilization) {
        TvPlayerSettingsHeader(stringResource(R.string.player_settings_section_sound))
    }
    if (showVolume) {
        TvPlayerSettingsStepperRow(
            label = stringResource(R.string.player_volume_title),
            value = "$volumePercent%",
            fraction = volumePercent.toFloat() / PlayerVolumeController.MAX_PERCENT,
            onStep = { direction ->
                onVolumeChange((volumePercent + direction).coerceIn(0, PlayerVolumeController.MAX_PERCENT))
            },
            onClick = {},
        )
    }
    if (showStabilization) {
        TvPlayerSettingsMenuRow(
            label = stringResource(R.string.player_volume_stabilization_title),
            value = stringResource(if (stabilizationEnabled) R.string.player_toggle_on else R.string.player_toggle_off),
            toggleChecked = stabilizationEnabled,
            onClick = { onStabilizationChange(!stabilizationEnabled) },
            showChevron = false,
        )
    }
}
