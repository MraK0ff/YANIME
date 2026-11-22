package su.afk.yummy.tv.feature.player.mobile.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import su.afk.yummy.tv.feature.player.mobile.model.MobileVideoTransform

internal const val MOBILE_PLAYER_MIN_VIDEO_SCALE = 1f
internal const val MOBILE_PLAYER_MAX_VIDEO_SCALE = 3f

internal fun calculateMobileVideoTransform(
    currentScale: Float,
    currentOffset: Offset,
    playerSize: IntSize,
    centroid: Offset,
    pan: Offset,
    zoomChange: Float,
): MobileVideoTransform {
    val newScale = (currentScale * zoomChange).coerceIn(
        MOBILE_PLAYER_MIN_VIDEO_SCALE,
        MOBILE_PLAYER_MAX_VIDEO_SCALE,
    )
    val nextOffset = if (newScale <= MOBILE_PLAYER_MIN_VIDEO_SCALE) {
        Offset.Zero
    } else {
        val center = Offset(playerSize.width / 2f, playerSize.height / 2f)
        val centroidFromCenter = centroid - center
        val scaleChange = newScale / currentScale
        ((currentOffset + centroidFromCenter) * scaleChange) - centroidFromCenter + pan
    }
    return MobileVideoTransform(
        scale = newScale,
        offset = nextOffset.clampedForMobileVideoScale(newScale, playerSize),
    )
}

internal fun MobileVideoTransform.clampedForPlayerSize(size: IntSize): MobileVideoTransform {
    // Stored offsets are in pixels and may outlive an orientation/window-size change.
    // Keep valid user zoom, but never let stale coordinates move the frame outside the viewport.
    val safeScale = scale.takeIf { it.isFinite() && it in MOBILE_PLAYER_MIN_VIDEO_SCALE..MOBILE_PLAYER_MAX_VIDEO_SCALE }
        ?: MOBILE_PLAYER_MIN_VIDEO_SCALE
    return copy(
        scale = safeScale,
        offset = offset.clampedForMobileVideoScale(safeScale, size),
    )
}

internal fun Offset.clampedForMobileVideoScale(scale: Float, size: IntSize): Offset {
    if (scale <= MOBILE_PLAYER_MIN_VIDEO_SCALE || size == IntSize.Zero) return Offset.Zero

    val maxX = (size.width * (scale - MOBILE_PLAYER_MIN_VIDEO_SCALE) / 2f).coerceAtLeast(0f)
    val maxY = (size.height * (scale - MOBILE_PLAYER_MIN_VIDEO_SCALE) / 2f).coerceAtLeast(0f)
    return Offset(
        x = x.coerceIn(-maxX, maxX),
        y = y.coerceIn(-maxY, maxY),
    )
}
