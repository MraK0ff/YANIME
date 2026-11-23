package su.afk.yummy.tv.feature.player.common.service

import android.os.Bundle
import androidx.media3.session.SessionCommand

/** Команды playback-сессии, общие для сервиса и подключённого к нему UI-контроллера. */
internal object PlayerSessionCommands {
    const val ACTION_STOP_SERVICE = "su.afk.yummy.tv.player.STOP_SERVICE"
    const val ACTION_SET_SCRUBBING_MODE = "su.afk.yummy.tv.player.SET_SCRUBBING_MODE"
    const val KEY_SCRUBBING_ENABLED = "scrubbing_enabled"

    val STOP_SERVICE = SessionCommand(ACTION_STOP_SERVICE, Bundle.EMPTY)
    val SET_SCRUBBING_MODE = SessionCommand(ACTION_SET_SCRUBBING_MODE, Bundle.EMPTY)
}
