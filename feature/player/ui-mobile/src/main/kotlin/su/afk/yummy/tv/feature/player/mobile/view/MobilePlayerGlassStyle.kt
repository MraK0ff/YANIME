package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared restrained player geometry: compact controls with one coherent visual language. */
internal val MobilePlayerInteractiveShape = RoundedCornerShape(16.dp)
internal val MobilePlayerStaticShape = RoundedCornerShape(10.dp)

internal const val MobilePlayerSelectedContainerAlpha = 0.12f
internal const val MobilePlayerUnselectedContainerAlpha = 0.34f
internal val MobilePlayerMinimumTouchTarget = 48.dp
internal val MobilePlayerGlassFill = Color(0xFF17191D).copy(alpha = 0.78f)
internal val MobilePlayerGlassBorder = Color.White.copy(alpha = 0.08f)

/** Subtle translucent surface: keep the video legible and avoid floating-card decoration. */
internal fun Modifier.mobilePlayerGlassSurface(
    shape: Shape = MobilePlayerStaticShape,
    fill: Color = MobilePlayerGlassFill,
    borderColor: Color = MobilePlayerGlassBorder,
    elevation: Dp = 0.dp,
): Modifier = shadow(elevation = elevation, shape = shape, clip = false)
    .clip(shape)
    .background(fill)
    .border(1.dp, borderColor, shape)

/**
 * Backward-compatible default for controls that do not receive the measured player width.
 * Prefer the width-aware overload whenever the parent has measured its available space.
 */
internal fun mobilePlayerControlButtonSize(primary: Boolean = false): Dp =
    mobilePlayerControlButtonSize(availableWidth = 400.dp, primary = primary)

internal fun mobilePlayerControlButtonSize(availableWidth: Dp, primary: Boolean = false): Dp =
    when {
        availableWidth >= 440.dp -> if (primary) 68.dp else MobilePlayerMinimumTouchTarget
        availableWidth >= 392.dp -> if (primary) 64.dp else MobilePlayerMinimumTouchTarget
        else -> if (primary) 60.dp else MobilePlayerMinimumTouchTarget
    }

/** Shared accent used by both the played timeline segment and the primary playback button. */
internal fun mobilePlayerTimelinePlayedColor(accent: Color): Color {
    val chroma = maxOf(accent.red, accent.green, accent.blue) -
        minOf(accent.red, accent.green, accent.blue)
    // Graphite uses a near-white neutral accent; soften it so the white buffer remains distinguishable.
    return if (accent.luminance() > 0.78f && chroma < 0.12f) {
        Color(0xFFB7BEC7)
    } else {
        accent
    }
}
