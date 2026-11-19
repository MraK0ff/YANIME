package su.afk.yummy.tv.feature.player.mobile.model

import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.core.model.settings.PlayerOrientationMode
import su.afk.yummy.tv.core.model.settings.PlayerSubtitleStyleSettings
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.model.settings.VideoEnhancementMode

/** Всё, что единое меню настроек показывает: значения пунктов и доступность необязательных. */
internal data class MobilePlayerSettingsMenuModel(
    val qualities: List<String>,
    val selectedQuality: String?,
    val adaptiveQuality: Boolean,
    val adaptiveLimitQuality: String?,
    val adaptiveLimitBest: Boolean,
    val speed: Float,
    val audioTrackNames: List<String>,
    val selectedAudioTrackIndex: Int,
    val showAudioTracks: Boolean,
    val dubbingNames: List<String>,
    val dubbingEpisodeCounts: List<Int>,
    val dubbingViews: List<Int>,
    val dubbingAvailability: List<Boolean>,
    val selectedDubbingIndex: Int,
    val dubbingBalancerNames: List<List<String>>,
    val dubbingBalancerIndices: List<List<Int>>,
    val selectedBalancerGraphIndex: Int,
    val balancerNames: List<String>,
    val balancerAvailability: List<Boolean>,
    val balancerExternal: List<Boolean>,
    val selectedBalancerIndex: Int,
    val subtitleTrackNames: List<String>,
    val selectedSubtitleTrackIndex: Int,
    val showSubtitleTracks: Boolean,
    val subtitleStyle: PlayerSubtitleStyleSettings,
    val resizeMode: PlayerResizeMode,
    val orientationMode: PlayerOrientationMode,
    val autoBrightness: Boolean,
    val showVolume: Boolean,
    val volumePercent: Int,
    val showStabilization: Boolean,
    val stabilizationEnabled: Boolean,
    /** Выбранный интервал автовыключения в минутах; 0 — таймер выключен. */
    val sleepTimerMinutes: Int,
    val showDetails: Boolean,
    val showPictureInPicture: Boolean,
    val pictureInPictureEnabled: Boolean,
    val timeDisplaySettings: PlayerTimeDisplaySettings,
    val videoEnhancementMode: VideoEnhancementMode,
    val showCast: Boolean,
)

/** Действия единого меню настроек. */
internal class MobilePlayerSettingsMenuActions(
    val onQualitySelected: (String) -> Unit,
    val onAdaptiveQualityChange: (Boolean) -> Unit,
    val onAdaptiveLimitSelected: (String) -> Unit,
    val onAdaptiveLimitBestSelected: () -> Unit,
    val onSpeedSelected: (Float) -> Unit,
    val onAudioTrackSelected: (Int) -> Unit,
    val onDubbingBalancerSelected: (dubbingIndex: Int, balancerGraphIndex: Int) -> Unit,
    val onBalancerSelected: (Int) -> Unit,
    val onSubtitleTrackSelected: (Int) -> Unit,
    val onSubtitleStyleChange: (PlayerSubtitleStyleSettings) -> Unit,
    val onResizeModeSelected: (PlayerResizeMode) -> Unit,
    val onOrientationModeSelected: (PlayerOrientationMode) -> Unit,
    val onAutoBrightnessChange: (Boolean) -> Unit,
    val onVolumeChange: (Int) -> Unit,
    val onStabilizationChange: (Boolean) -> Unit,
    val onSleepTimerSelected: (Int) -> Unit,
    val onDetails: () -> Unit,
    val onPictureInPictureChange: (Boolean) -> Unit,
    val onTimeDisplaySettingsChange: (PlayerTimeDisplaySettings) -> Unit,
    val onVideoEnhancementModeChange: (VideoEnhancementMode) -> Unit,
)
