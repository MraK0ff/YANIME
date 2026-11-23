package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.balancerFallbackIcon
import su.afk.yummy.tv.core.designsystem.utils.balancerLogoRes
import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.model.settings.PlayerZoomLevel
import su.afk.yummy.tv.feature.player.model.PanelReturnFocusTarget
import su.afk.yummy.tv.feature.player.model.PlayerPlaybackUiState
import su.afk.yummy.tv.feature.player.model.TvPlayerFocusRequesters
import su.afk.yummy.tv.feature.player.model.TvPlayerPanel
import su.afk.yummy.tv.feature.player.model.TvPlayerPanelsState
import su.afk.yummy.tv.feature.player.presentation.R
import su.afk.yummy.tv.feature.player.utils.speedLabel

/** Панели качества, озвучки, субтитров, балансера, Alloha и настроек плеера. */
@Composable
internal fun BoxScope.TvPlayerPanelsHost(
    panels: TvPlayerPanelsState,
    focus: TvPlayerFocusRequesters,
    playback: PlayerPlaybackUiState,
    qualities: List<String>,
    activeQuality: String?,
    speeds: List<Float>,
    activeSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    resizeMode: PlayerResizeMode,
    zoomLevel: PlayerZoomLevel,
    timeDisplaySettings: PlayerTimeDisplaySettings,
    volumePercent: Int,
    showVolume: Boolean,
    showStabilization: Boolean,
    stabilizationEnabled: Boolean,
    adaptiveQualityEnabled: Boolean,
    audioTrackNames: List<String>,
    selectedAudioTrackIndex: Int,
    subtitleTrackNames: List<String>,
    selectedSubtitleTrackIndex: Int,
    onQualitySelected: (index: Int) -> Unit,
    onDubbingSelected: (index: Int) -> Unit,
    onBalancerSelected: (index: Int) -> Unit,
    onResizeModeSelected: (PlayerResizeMode) -> Unit,
    onZoomLevelSelected: (PlayerZoomLevel) -> Unit,
    onTimeDisplaySettingsChange: (PlayerTimeDisplaySettings) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onStabilizationChange: (Boolean) -> Unit,
    onAdaptiveQualityChange: (Boolean) -> Unit,
    onAudioTrackSelected: (index: Int) -> Unit,
    onSubtitleTrackSelected: (index: Int) -> Unit,
    onExitPanelDown: (PanelReturnFocusTarget) -> Unit,
) {
    TvPlayerSelectionPanel(
        visible = panels.isOpen(TvPlayerPanel.Speed),
        title = stringResource(R.string.player_speed_title),
        items = speeds.map { it.speedLabel() },
        selectedIndex = speeds.indexOf(activeSpeed).coerceAtLeast(0),
        selectedFocusRequester = focus.selectedSpeed,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 48.dp, bottom = 72.dp),
        itemMeta = { null },
        onItemSelected = { index ->
            speeds.getOrNull(index)?.let(onSpeedSelected)
        },
        onExitDown = { onExitPanelDown(PanelReturnFocusTarget.Speed) },
    )

    TvPlayerSelectionPanel(
        visible = panels.isOpen(TvPlayerPanel.Quality),
        title = stringResource(R.string.player_quality_title),
        items = qualities,
        selectedIndex = qualities.indexOf(activeQuality),
        selectedFocusRequester = focus.selectedQuality,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 48.dp, bottom = 72.dp),
        headerContent = if (qualities.size > 1) {
            {
                TvPlayerSettingsMenuRow(
                    label = stringResource(R.string.player_adaptive_quality_title),
                    value = stringResource(if (adaptiveQualityEnabled) R.string.player_toggle_on else R.string.player_toggle_off),
                    toggleChecked = adaptiveQualityEnabled,
                    showChevron = false,
                    onClick = { onAdaptiveQualityChange(!adaptiveQualityEnabled) },
                )
            }
        } else {
            null
        },
        itemMeta = { stringResource(R.string.player_quality_meta) },
        onItemSelected = onQualitySelected,
        onExitDown = { onExitPanelDown(PanelReturnFocusTarget.Quality) },
    )

    TvDubbingSelectionPanel(
        visible = panels.isOpen(TvPlayerPanel.Dubbing),
        playback = playback,
        selectedFocusRequester = focus.selectedDubbing,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 48.dp, bottom = 72.dp),
        onItemSelected = onDubbingSelected,
        onExitDown = { onExitPanelDown(PanelReturnFocusTarget.Dubbing) },
    )

    TvPlayerSelectionPanel(
        visible = panels.isOpen(TvPlayerPanel.Subtitles),
        title = stringResource(R.string.player_subtitle_track_title),
        items = subtitleTrackNames,
        selectedIndex = selectedSubtitleTrackIndex,
        selectedFocusRequester = focus.selectedSubtitles,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 48.dp, bottom = 72.dp),
        onItemSelected = onSubtitleTrackSelected,
        onExitDown = { onExitPanelDown(PanelReturnFocusTarget.Subtitles) },
    )

    TvPlayerSelectionPanel(
        visible = panels.isOpen(TvPlayerPanel.Balancer),
        title = stringResource(R.string.player_balancer_name_prefix),
        items = playback.balancerNames,
        selectedIndex = playback.currentBalancerIndex,
        selectedFocusRequester = focus.selectedBalancer,
        enabledItems = playback.balancerAvailability,
        accentLabel = true,
        logoFor = ::balancerLogoRes,
        logoFallbackFor = ::balancerFallbackIcon,
        disabledItemMeta = stringResource(R.string.player_episode_unavailable),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 48.dp, bottom = 72.dp),
        itemMeta = { index ->
            stringResource(
                if (playback.balancerExternal.getOrElse(index) { false }) {
                    R.string.player_balancer_external_meta
                } else {
                    R.string.player_balancer_meta
                },
            )
        },
        onItemSelected = onBalancerSelected,
        onExitDown = { onExitPanelDown(PanelReturnFocusTarget.Balancer) },
    )

    TvPlayerSettingsPanel(
        visible = panels.isOpen(TvPlayerPanel.PlayerSettings),
        entryFocusRequester = focus.selectedPlayerSettings,
        resizeMode = resizeMode,
        zoomLevel = zoomLevel,
        onResizeModeSelected = onResizeModeSelected,
        onZoomLevelSelected = onZoomLevelSelected,
        timeDisplaySettings = timeDisplaySettings,
        onTimeDisplaySettingsChange = onTimeDisplaySettingsChange,
        showVolume = showVolume,
        volumePercent = volumePercent,
        onVolumeChange = onVolumeChange,
        showStabilization = showStabilization,
        stabilizationEnabled = stabilizationEnabled,
        onStabilizationChange = onStabilizationChange,
        onClose = { onExitPanelDown(PanelReturnFocusTarget.PlayerSettings) },
        modifier = Modifier.align(Alignment.CenterEnd),
    )

    TvPlayerAllohaPanel(
        visible = panels.isOpen(TvPlayerPanel.Alloha),
        audioTrackNames = audioTrackNames,
        selectedAudioTrackIndex = selectedAudioTrackIndex,
        onAudioTrackSelected = onAudioTrackSelected,
        selectedFocusRequester = focus.selectedAlloha,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 48.dp, bottom = 72.dp),
        onExitDown = { onExitPanelDown(PanelReturnFocusTarget.Alloha) },
    )
}
