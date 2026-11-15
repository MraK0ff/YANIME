package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared player shapes: controls read as a coherent dock, not a collection of floating bubbles. */
internal val MobilePlayerInteractiveShape = RoundedCornerShape(18.dp)
internal val MobilePlayerStaticShape = RoundedCornerShape(12.dp)

internal const val MobilePlayerSelectedContainerAlpha = 0.14f
internal const val MobilePlayerUnselectedContainerAlpha = 0.46f
internal val MobilePlayerMinimumTouchTarget = 48.dp
internal val MobilePlayerGlassFill = Color(0xFF11151B).copy(alpha = 0.72f)
internal val MobilePlayerGlassBorder = Color.White.copy(alpha = 0.12f)

/** Restrained translucent surface: no heavy shadow over video content. */
internal fun Modifier.mobilePlayerGlassSurface(
    shape: Shape = MobilePlayerStaticShape,
    fill: Color = MobilePlayerGlassFill,
    borderColor: Color = MobilePlayerGlassBorder,
    elevation: Dp = 0.dp,
): Modifier = shadow(elevation = elevation, shape = shape, clip = false)
    .clip(shape)
    .background(fill)
    .border(1.dp, borderColor, shape)

internal fun mobilePlayerControlButtonSize(availableWidth: Dp, primary: Boolean = false): Dp =
    when {
        availableWidth >= 440.dp -> if (primary) 68.dp else MobilePlayerMinimumTouchTarget
        availableWidth >= 392.dp -> if (primary) 64.dp else MobilePlayerMinimumTouchTarget
        else -> if (primary) 60.dp else MobilePlayerMinimumTouchTarget
    }
