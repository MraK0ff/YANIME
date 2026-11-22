package su.afk.yummy.tv.feature.player.model

internal enum class PlayerControlFocusTarget {
    Dubbing,
    Balancer,
    Episodes,
    Quality,
    Speed,
    Subtitles,

    /** Боковое меню настроек справа: масштаб, время на экране и звук. */
    PlayerSettings,
    Alloha,

    /** Кнопка «следующая серия» в нижнем ряду. */
    NextEpisode,
}
