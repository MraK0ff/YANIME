package su.afk.yummy.tv.feature.player.view.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.focus.requestFocusUntilTimeout
import su.afk.yummy.tv.core.designsystem.theme.YummySemanticColors
import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.model.settings.PlayerZoomLevel
import su.afk.yummy.tv.feature.player.model.TvPlayerSettingsPage

/**
 * Боковое меню настроек ТВ-плеера: масштаб, время на экране и звук.
 * Скорость остаётся в быстром управлении, субтитры вынесены в отдельную панель.
 */
@Composable
internal fun TvPlayerSettingsPanel(
    visible: Boolean,
    entryFocusRequester: FocusRequester,
    resizeMode: PlayerResizeMode,
    zoomLevel: PlayerZoomLevel,
    onResizeModeSelected: (PlayerResizeMode) -> Unit,
    onZoomLevelSelected: (PlayerZoomLevel) -> Unit,
    timeDisplaySettings: PlayerTimeDisplaySettings,
    onTimeDisplaySettingsChange: (PlayerTimeDisplaySettings) -> Unit,
    showVolume: Boolean,
    volumePercent: Int,
    onVolumeChange: (Int) -> Unit,
    showStabilization: Boolean,
    stabilizationEnabled: Boolean,
    onStabilizationChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInHorizontally { it } + fadeIn(),
        exit = slideOutHorizontally { it } + fadeOut(),
    ) {
        var page by remember { mutableStateOf<TvPlayerSettingsPage?>(null) }
        var returnPage by remember { mutableStateOf<TvPlayerSettingsPage?>(null) }
        val rowFocus = remember { TvPlayerSettingsPage.entries.associateWith { FocusRequester() } }
        val optionFocus = remember { FocusRequester() }

        fun openPage(target: TvPlayerSettingsPage) {
            returnPage = target
            page = target
        }

        LaunchedEffect(page) {
            val current = page
            if (current != null) {
                requestFocusUntilTimeout(optionFocus)
            } else {
                returnPage?.let { requestFocusUntilTimeout(rowFocus.getValue(it)) }
            }
        }

        BackHandler(enabled = page != null) { page = null }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
                .background(
                    YummySemanticColors.PanelScrim.copy(alpha = 0.92f),
                    RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                )
                .border(
                    1.dp,
                    androidx.compose.ui.graphics.Color.White.copy(alpha = 0.10f),
                    RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                )
                .focusProperties {
                    onExit = {
                        when (requestedFocusDirection) {
                            FocusDirection.Left -> if (page != null) {
                                page = null
                                cancelFocusChange()
                            } else {
                                onClose()
                            }
                            FocusDirection.Up, FocusDirection.Down, FocusDirection.Right -> cancelFocusChange()
                            else -> Unit
                        }
                    }
                }
                .focusGroup()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (val current = page) {
                null -> TvPlayerSettingsRoot(
                    entryFocusRequester = entryFocusRequester,
                    rowFocus = rowFocus,
                    resizeMode = resizeMode,
                    zoomLevel = zoomLevel,
                    showVolume = showVolume,
                    volumePercent = volumePercent,
                    onVolumeChange = onVolumeChange,
                    showStabilization = showStabilization,
                    stabilizationEnabled = stabilizationEnabled,
                    onStabilizationChange = onStabilizationChange,
                    onOpenPage = ::openPage,
                )
                else -> TvPlayerSettingsOptionsPage(
                    page = current,
                    optionFocus = optionFocus,
                    resizeMode = resizeMode,
                    zoomLevel = zoomLevel,
                    onResizeModeSelected = onResizeModeSelected,
                    onZoomLevelSelected = onZoomLevelSelected,
                    timeDisplaySettings = timeDisplaySettings,
                    onTimeDisplaySettingsChange = onTimeDisplaySettingsChange,
                    onDone = { page = null },
                )
            }
        }
    }
}
