package su.afk.yummy.tv.feature.player.mobile.utils

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

internal const val MOBILE_PLAYER_SPEED_MIN = 0.25f
internal const val MOBILE_PLAYER_SPEED_MAX = 3f
private const val MOBILE_PLAYER_SPEED_STEP = 0.05f

/** Быстрые значения скорости, как в плеере YouTube. */
internal val MobilePlayerSpeedChips: List<Float> =
    listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f)

/** Скорость в пределах диапазона и с шагом 0.05: float-хвосты ползунка не попадают в настройки. */
internal fun Float.toMobilePlayerSpeed(): Float =
    ((this.coerceIn(MOBILE_PLAYER_SPEED_MIN, MOBILE_PLAYER_SPEED_MAX) / MOBILE_PLAYER_SPEED_STEP).roundToInt() *
        MOBILE_PLAYER_SPEED_STEP).let { (it * 100f).roundToInt() / 100f }

/** Нормальная скорость всегда подписана однозначно как 1x. */
internal fun Float.toSpeedTitle(): String =
    if (abs(this - 1f) < 0.001f) "1x" else String.format(Locale.getDefault(), "%.2fx", this)

/** Компактные подписи пресетов; 1x явно обозначает нормальную скорость. */
internal fun Float.toSpeedChipLabel(): String =
    if (abs(this - 1f) < 0.001f) "1x"
    else String.format(Locale.getDefault(), "%.2f", this)
        .trimEnd('0').let { if (it.endsWith(',') || it.endsWith('.')) it + "0" else it }

/** Для строки меню: «1x», «1,5x». */
internal fun Float.toSpeedMenuValue(): String =
    if (abs(this - 1f) < 0.001f) "1x" else "${toSpeedChipLabel()}x"

internal const val MOBILE_PLAYER_SPEED_SLIDER_STEPS = 54
