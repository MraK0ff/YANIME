package su.afk.yummy.tv.feature.player.model

internal enum class PanelReturnFocusTarget {
    Quality,
    Speed,
    Subtitles,
    Dubbing,
    Balancer,
    Episodes,
    /** Боковое меню настроек справа: скорость, масштаб, субтитры и звук. */
    PlayerSettings,
    Alloha,
    NextEpisode,
}
