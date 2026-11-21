package su.afk.yummy.tv.feature.player.common.service

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.media3.session.MediaController

/** Сохраняет прежний mobile API: отключение контроллера не останавливает сервис автоматически. */
@Composable
fun rememberPlayerMediaController(): MediaController? =
    rememberPlayerPlaybackSessionClient().player

/** Requests ExoPlayer's optimized frame-preview mode for the duration of a timeline drag. */
fun MediaController.setTimelineScrubbingMode(enabled: Boolean) {
    runCatching {
        sendCustomCommand(
            PlayerSessionCommands.SET_SCRUBBING_MODE,
            Bundle().apply { putBoolean(PlayerSessionCommands.KEY_SCRUBBING_ENABLED, enabled) },
        )
    }
}
