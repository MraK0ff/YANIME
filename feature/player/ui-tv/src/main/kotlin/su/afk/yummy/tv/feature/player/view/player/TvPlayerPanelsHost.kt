package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.designsystem.utils.dubbingLogoRes
import su.afk.yummy.tv.core.designsystem.utils.balancerLogoRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.balancerFallbackIcon
import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.model.settings.PlayerZoomLevel
import su.afk.yummy.tv.feature.player.common.utils.formatCompactCount
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

    val dubbingPrefix = stringResource(R.string.player_dubbing_title)
    val balancerPrefix = stringResource(R.string.player_balancer_name_prefix)
    val combinedTrackItems = playback.dubbingNames.map { "$dubbingPrefix · ${it.dubbingDisplayName()}" } +
        playback.balancerNames.map { "$balancerPrefix · $it" }
    val dubbingCount = playback.dubbingNames.size
    val combinedTrackSelectedIndex = if (panels.isOpen(TvPlayerPanel.Balancer)) {
        (dubbingCount + playback.currentBalancerIndex).coerceIn(0, combinedTrackItems.lastIndex.coerceAtLeast(0))
    } else {
        playback.currentDubbingIndex.coerceIn(0, combinedTrackItems.lastIndex.coerceAtLeast(0))
    }
    TvPlayerSelectionPanel(
        visible = panels.isOpen(TvPlayerPanel.Dubbing) || panels.isOpen(TvPlayerPanel.Balancer),
        title = stringResource(R.string.player_dubbing_title) + " · " + balancerPrefix,
        items = combinedTrackItems,
        selectedIndex = combinedTrackSelectedIndex,
        selectedFocusRequester = if (panels.isOpen(TvPlayerPanel.Balancer)) focus.selectedBalancer else focus.selectedDubbing,
        enabledItems = playback.dubbingAvailability + playback.balancerAvailability,
        accentLabel = true,
        logoFor = { label ->
            if (label.startsWith("$dubbingPrefix · ")) {
                dubbingLogoRes(label.removePrefix("$dubbingPrefix · "))
            } else {
                balancerLogoRes(label.removePrefix("$balancerPrefix · "))
            }
        },
        logoFallbackFor = { label ->
            if (label.startsWith("$dubbingPrefix · ")) Icons.Filled.Mic else balancerFallbackIcon(label.removePrefix("$balancerPrefix · "))
        },
        itemMetaContent = { index, contentColor ->
            if (index < dubbingCount) {
                TvDubbingMetaRow(
                    views = playback.dubbingViews.getOrElse(index) { 0 }.formatCompactCount(),
                    episodeCount = playback.dubbingEpisodeCounts.getOrElse(index) { 0 },
                    sourceNames = playback.dubbingSourceNames.getOrElse(index) { "" },
                    contentColor = contentColor,
                )
            } else {
                val balancerIndex = index - dubbingCount
                Text(
                    text = stringResource(
                        if (playback.balancerExternal.getOrElse(balancerIndex) { false }) {
                            R.string.player_balancer_external_meta
                        } else {
                            R.string.player_balancer_meta
                        },
                    ),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 48.dp, bottom = 72.dp),
        onItemSelected = { index ->
            if (index < dubbingCount) onDubbingSelected(index) else onBalancerSelected(index - dubbingCount)
        },
        onExitDown = {
            onExitPanelDown(if (panels.isOpen(TvPlayerPanel.Balancer)) PanelReturnFocusTarget.Balancer else PanelReturnFocusTarget.Dubbing)
        },
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
