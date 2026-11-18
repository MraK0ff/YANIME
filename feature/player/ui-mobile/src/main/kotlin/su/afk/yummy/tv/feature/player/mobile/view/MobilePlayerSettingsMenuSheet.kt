package su.afk.yummy.tv.feature.player.mobile.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.baseScreen.HideSheetWindowSystemBars
import su.afk.yummy.tv.feature.player.mobile.model.MobilePlayerSettingsMenuActions
import su.afk.yummy.tv.feature.player.mobile.model.MobilePlayerSettingsMenuModel
import su.afk.yummy.tv.feature.player.mobile.model.MobilePlayerSettingsPage
import su.afk.yummy.tv.feature.player.presentation.R

/**
 * Единое меню настроек воспроизведения с приоритетом часто используемых параметров и вложенными страницами.
 * Все изменения применяются сразу и используют те же настройки, что и экран «Настройки».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerSettingsMenuSheet(
    model: MobilePlayerSettingsMenuModel,
    actions: MobilePlayerSettingsMenuActions,
    onDismiss: () -> Unit,
    /** Страница, на которой меню открывается: нижняя панель ведёт сразу на «Скорость». */
    initialPage: MobilePlayerSettingsPage = MobilePlayerSettingsPage.Root,
) {
    var pageStack by rememberSaveable { mutableStateOf(listOf(initialPage)) }
    val page = pageStack.last()
    androidx.compose.runtime.LaunchedEffect(initialPage) {
        pageStack = listOf(initialPage)
    }
    val goBack = {
        if (pageStack.size > 1) {
            pageStack = pageStack.dropLast(1)
        } else {
            onDismiss()
        }
    }
    val navigate: (MobilePlayerSettingsPage) -> Unit = { nextPage ->
        pageStack = pageStack + nextPage
    }
    BaseBottomSheet(
        onDismissRequest = onDismiss,
        scrollableContent = true,
        dragHandle = { MobilePlayerPanelDragTitle(stringResource(R.string.player_mobile_playback_settings_title)) },
    ) {
        HideSheetWindowSystemBars()
        // Back follows the actual nested page stack; direct toolbar entries close in one step.
        BackHandler(enabled = pageStack.size > 1, onBack = goBack)
        when (page) {
            MobilePlayerSettingsPage.Root -> MobilePlayerMenuRoot(
                model = model,
                actions = actions,
                onNavigate = navigate,
                onDismiss = onDismiss,
            )

            MobilePlayerSettingsPage.Quality -> MobilePlayerQualityPage(model, actions, goBack)
            MobilePlayerSettingsPage.Speed -> MobilePlayerSpeedPage(
                speed = model.speed,
                onSpeedSelected = actions.onSpeedSelected,
                onBack = goBack,
            )

            MobilePlayerSettingsPage.Subtitles -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MobilePlayerMenuPageHeader(
                    title = stringResource(R.string.player_menu_subtitles),
                    onBack = goBack,
                )
                if (model.showSubtitleTracks) {
                    MobilePlayerSubtitleTrackSection(
                        trackNames = model.subtitleTrackNames,
                        selectedIndex = model.selectedSubtitleTrackIndex,
                        onTrackSelected = actions.onSubtitleTrackSelected,
                    )
                }
                MobilePlayerSubtitleStyleSection(
                    style = model.subtitleStyle,
                    onStyleChange = actions.onSubtitleStyleChange,
                )
            }

            MobilePlayerSettingsPage.AudioTrack -> Column(modifier = Modifier.fillMaxWidth()) {
                MobilePlayerMenuPageHeader(
                    title = stringResource(R.string.player_menu_audio_track),
                    onBack = goBack,
                )
                MobilePlayerAudioTrackSection(
                    trackNames = model.audioTrackNames,
                    selectedIndex = model.selectedAudioTrackIndex,
                    onTrackSelected = actions.onAudioTrackSelected,
                )
            }

            MobilePlayerSettingsPage.Sources -> MobilePlayerSourcesPage(
                dubbingNames = model.dubbingNames,
                dubbingEpisodeCounts = model.dubbingEpisodeCounts,
                dubbingViews = model.dubbingViews,
                dubbingAvailability = model.dubbingAvailability,
                selectedDubbingIndex = model.selectedDubbingIndex,
                onDubbingSelected = actions.onDubbingSelected,
                balancerNames = model.balancerNames,
                balancerAvailability = model.balancerAvailability,
                balancerExternal = model.balancerExternal,
                selectedBalancerIndex = model.selectedBalancerIndex,
                onBalancerSelected = actions.onBalancerSelected,
                onBack = goBack,
            )

            MobilePlayerSettingsPage.SleepTimer -> MobilePlayerSleepTimerPage(
                selectedMinutes = model.sleepTimerMinutes,
                onSelected = actions.onSleepTimerSelected,
                onBack = goBack,
            )

            MobilePlayerSettingsPage.Resize -> Column(modifier = Modifier.fillMaxWidth()) {
                MobilePlayerMenuPageHeader(
                    title = stringResource(R.string.player_resize_title),
                    onBack = goBack,
                )
                MobilePlayerScaleSelector(
                    selectedMode = model.resizeMode,
                    onModeSelected = actions.onResizeModeSelected,
                )
            }

            MobilePlayerSettingsPage.Orientation -> MobilePlayerOrientationPage(
                selected = model.orientationMode,
                onSelected = actions.onOrientationModeSelected,
                onBack = goBack,
            )

            MobilePlayerSettingsPage.CastAndPip -> MobilePlayerCastPipPage(
                showPictureInPicture = model.showPictureInPicture,
                showCast = model.showCast,
                pictureInPictureEnabled = model.pictureInPictureEnabled,
                onPictureInPictureChange = actions.onPictureInPictureChange,
                onBack = goBack,
            )

            MobilePlayerSettingsPage.TimeDisplay -> MobilePlayerTimeDisplayPage(
                settings = model.timeDisplaySettings,
                onSettingsChange = actions.onTimeDisplaySettingsChange,
                onBack = goBack,
            )

            MobilePlayerSettingsPage.VideoEnhancement -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MobilePlayerMenuPageHeader(
                    title = stringResource(R.string.player_video_enhancement_title),
                    onBack = goBack,
                )
                listOf(
                    su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.OFF to R.string.player_toggle_off,
                    su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.SHARPEN to R.string.player_video_enhancement_sharpen,
                    su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.QUALITY to R.string.player_video_enhancement_quality,
                ).forEach { (mode, labelRes) ->
                    MobilePlayerMenuRow(
                        label = stringResource(labelRes),
                        subtitle = if (mode == su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.QUALITY) {
                            stringResource(R.string.player_video_enhancement_hint)
                        } else null,
                        trailing = MobilePlayerMenuRowTrailing.Choice(model.videoEnhancementMode == mode),
                        onClick = {
                            actions.onVideoEnhancementModeChange(mode)
                            goBack()
                        },
                    )
                }
            }

            MobilePlayerSettingsPage.Display -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MobilePlayerMenuPageHeader(
                    title = stringResource(R.string.player_menu_display),
                    onBack = goBack,
                )
                MobilePlayerMenuRow(
                    icon = Icons.Filled.ScreenRotation,
                    label = stringResource(R.string.player_mobile_orientation),
                    trailing = MobilePlayerMenuRowTrailing.Navigate(),
                    onClick = { navigate(MobilePlayerSettingsPage.Orientation) },
                )
                MobilePlayerMenuRow(
                    label = stringResource(R.string.player_video_enhancement_title),
                    subtitle = stringResource(R.string.player_video_enhancement_hint),
                    trailing = MobilePlayerMenuRowTrailing.Navigate(
                        value = stringResource(
                            when (model.videoEnhancementMode) {
                                su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.OFF -> R.string.player_toggle_off
                                su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.SHARPEN -> R.string.player_video_enhancement_sharpen
                                su.afk.yummy.tv.core.model.settings.VideoEnhancementMode.QUALITY -> R.string.player_video_enhancement_quality
                            },
                        ),
                    ),
                    onClick = { navigate(MobilePlayerSettingsPage.VideoEnhancement) },
                )
                MobilePlayerMenuRow(
                    icon = Icons.Filled.BrightnessMedium,
                    label = stringResource(R.string.player_brightness_auto_title),
                    subtitle = stringResource(R.string.player_brightness_auto_hint),
                    trailing = MobilePlayerMenuRowTrailing.Toggle(model.autoBrightness),
                    onClick = { actions.onAutoBrightnessChange(!model.autoBrightness) },
                )
                if (model.showVolume) {
                    // Стабилизация - отдельной строкой ниже, секция тут только про независимую громкость.
                    MobilePlayerVolumeSection(
                        showVolume = true,
                        volumePercent = model.volumePercent,
                        onVolumeChange = actions.onVolumeChange,
                        showStabilization = false,
                        stabilizationEnabled = model.stabilizationEnabled,
                        onStabilizationChange = actions.onStabilizationChange,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
                if (model.showStabilization) {
                    MobilePlayerMenuRow(
                        icon = Icons.Filled.GraphicEq,
                        label = stringResource(R.string.player_volume_stabilization_title),
                        trailing = MobilePlayerMenuRowTrailing.Toggle(model.stabilizationEnabled),
                        onClick = { actions.onStabilizationChange(!model.stabilizationEnabled) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MobilePlayerMenuRoot(
    model: MobilePlayerSettingsMenuModel,
    actions: MobilePlayerSettingsMenuActions,
    onNavigate: (MobilePlayerSettingsPage) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // First show settings that do not have a dedicated shortcut on the player surface.
        if (model.showAudioTracks) {
            MobilePlayerMenuRow(
                icon = Icons.Filled.Audiotrack,
                label = stringResource(R.string.player_menu_audio_track),
                trailing = MobilePlayerMenuRowTrailing.Navigate(
                    model.audioTrackNames.getOrNull(model.selectedAudioTrackIndex),
                ),
                onClick = { onNavigate(MobilePlayerSettingsPage.AudioTrack) },
            )
        }
        if (model.showSubtitleTracks) {
            MobilePlayerMenuRow(
                icon = Icons.Filled.Subtitles,
                label = stringResource(R.string.player_menu_subtitles),
                trailing = MobilePlayerMenuRowTrailing.Navigate(
                    model.subtitleTrackNames.getOrNull(model.selectedSubtitleTrackIndex),
                ),
                onClick = { onNavigate(MobilePlayerSettingsPage.Subtitles) },
            )
        }
        if (model.showDetails) {
            MobilePlayerMenuRow(
                icon = Icons.Filled.Info,
                label = stringResource(R.string.player_mobile_details),
                trailing = MobilePlayerMenuRowTrailing.None,
                onClick = {
                    onDismiss()
                    actions.onDetails()
                },
            )
        }
        MobilePlayerMenuRow(
            icon = Icons.Filled.Bedtime,
            label = stringResource(R.string.player_sleep_title),
            trailing = MobilePlayerMenuRowTrailing.Navigate(
                if (model.sleepTimerMinutes > 0) sleepTimerLabel(model.sleepTimerMinutes)
                else stringResource(R.string.player_sleep_off),
            ),
            onClick = { onNavigate(MobilePlayerSettingsPage.SleepTimer) },
        )
        MobilePlayerMenuRow(
            icon = Icons.Filled.AspectRatio,
            label = stringResource(R.string.player_resize_title),
            trailing = MobilePlayerMenuRowTrailing.Navigate(),
            onClick = { onNavigate(MobilePlayerSettingsPage.Resize) },
        )
        MobilePlayerMenuRow(
            icon = Icons.Filled.AccessTime,
            label = stringResource(R.string.player_time_display_title),
            trailing = MobilePlayerMenuRowTrailing.Navigate(),
            onClick = { onNavigate(MobilePlayerSettingsPage.TimeDisplay) },
        )
        MobilePlayerMenuRow(
            icon = Icons.Filled.BrightnessMedium,
            label = stringResource(R.string.player_menu_brightness_volume),
            trailing = MobilePlayerMenuRowTrailing.Navigate(),
            onClick = { onNavigate(MobilePlayerSettingsPage.Display) },
        )
        if (model.showPictureInPicture || model.showCast) {
            MobilePlayerMenuRow(
                icon = Icons.Filled.Cast,
                label = stringResource(R.string.player_menu_cast_pip),
                trailing = MobilePlayerMenuRowTrailing.Navigate(),
                onClick = { onNavigate(MobilePlayerSettingsPage.CastAndPip) },
            )
        }



    }
}
