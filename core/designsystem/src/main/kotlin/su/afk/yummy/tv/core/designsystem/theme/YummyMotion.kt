package su.afk.yummy.tv.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp

/**
 * Motion tokens for bespoke YANIME interactions. Material components use the expressive
 * MaterialTheme motion scheme; these specs keep custom focus and selection animations aligned.
 * Callers that animate custom UI also gate these specs with rememberAnimationsEnabled().
 */
object YummyMotion {
    /** TV focus should feel responsive without making the focused target wobble. */
    val TvFocusScale: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Responsive width transition for the TV side navigation panel. */
    val TvSideMenuWidth: SpringSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** A slightly more playful spring reserved for deliberate selection/expansion feedback. */
    val Selection: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    const val FastEffectsMillis: Int = 150
    const val StandardEffectsMillis: Int = 220
    const val EmphasizedEffectsMillis: Int = 320
}
