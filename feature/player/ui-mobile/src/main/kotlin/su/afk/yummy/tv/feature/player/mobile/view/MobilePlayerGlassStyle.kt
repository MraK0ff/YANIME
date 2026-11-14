package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared shape tokens: interactive surfaces are rounder than passive labels. */
internal val MobilePlayerInteractiveShape = RoundedCornerShape(16.dp)
internal val MobilePlayerStaticShape = RoundedCornerShape(10.dp)

/** Shared frosted-glass treatment for player chips and controls. */
internal const val MobilePlayerSelectedContainerAlpha = 0.14f
internal const val MobilePlayerUnselectedContainerAlpha = 0.46f
internal val MobilePlayerMinimumTouchTarget = 48.dp
internal val MobilePlayerGlassFill = Color(0xFF101318).copy(alpha = 0.42f)
internal val MobilePlayerGlassBorder = Color.White.copy(alpha = 0.18f)

internal fun Modifier.mobilePlayerGlassSurface(
    shape: Shape = MobilePlayerStaticShape,
    fill: Color = MobilePlayerGlassFill,
    borderColor: Color = MobilePlayerGlassBorder,
    elevation: Dp = 12.dp,
): Modifier = shadow(elevation = elevation, shape = shape, clip = false)
    .clip(shape)
    .background(fill)
    .border(1.dp, borderColor, shape)

@androidx.compose.runtime.Composable
internal fun mobilePlayerControlButtonSize(primary: Boolean = false): Dp {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp >= 440 -> if (primary) 68.dp else MobilePlayerMinimumTouchTarget
        widthDp >= 392 -> if (primary) 64.dp else MobilePlayerMinimumTouchTarget
        else -> if (primary) 60.dp else MobilePlayerMinimumTouchTarget
    }
}
