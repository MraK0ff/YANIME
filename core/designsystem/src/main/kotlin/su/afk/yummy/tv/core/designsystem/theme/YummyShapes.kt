package su.afk.yummy.tv.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Complete Material 3 Expressive shape scale. Increased sizes are explicit so the
 * design system does not silently fall back to library defaults for prominent surfaces.
 */
val YummyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
    largeIncreased = RoundedCornerShape(24.dp),
    extraLargeIncreased = RoundedCornerShape(32.dp),
    extraExtraLarge = RoundedCornerShape(40.dp),
)

/** Named geometry tokens for bespoke YANIME components that are not Material components. */
object YummyShapeTokens {
    val Compact: Shape = YummyShapes.small
    val Control: Shape = RoundedCornerShape(16.dp)
    val Card: Shape = YummyShapes.large
    val ProminentCard: Shape = YummyShapes.largeIncreased
    val Dialog: Shape = YummyShapes.extraLargeIncreased
    val HeroSurface: Shape = YummyShapes.extraExtraLarge
    val Pill: Shape = CircleShape
}
