package su.afk.yummy.tv.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.PI

/**
 * Shared M3 Expressive-inspired playback track.
 *
 * The played section is a smooth wave which returns to the exact track baseline at its end.
 * Buffering starts at that same coordinate, so played and upcoming portions meet without a
 * visual gap or a detached thumb/segment.
 */
fun DrawScope.drawExpressiveWavyTimelineTrack(
    progress: Float,
    bufferedProgress: Float,
    playedColor: Color,
    focused: Boolean = false,
    inactiveColor: Color = Color.White.copy(alpha = 0.20f),
    bufferedColor: Color = Color.White.copy(alpha = 0.34f),
    wavePhase: Float = 0f,
) {
    if (size.width <= 0f || size.height <= 0f) return

    val safeProgress = progress.coerceIn(0f, 1f)
    val safeBuffered = bufferedProgress.coerceIn(safeProgress, 1f)
    val centerY = size.height / 2f
    val playedX = size.width * safeProgress
    val bufferedX = size.width * safeBuffered
    val baseWidth = if (focused) 4.dp.toPx() else 3.dp.toPx()
    val playedWidth = if (focused) 5.dp.toPx() else 4.dp.toPx()

    // Draw only the unplayed rail. The played wave replaces its section completely,
    // so no straight line can peek through underneath the snake.
    if (playedX < size.width) {
        drawLine(
            color = inactiveColor,
            start = Offset(playedX, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = baseWidth,
            cap = StrokeCap.Round,
        )
    }

    // Buffered progress starts exactly at the snake's endpoint.
    if (bufferedX > playedX) {
        drawLine(
            color = bufferedColor,
            start = Offset(playedX, centerY),
            end = Offset(bufferedX, centerY),
            strokeWidth = baseWidth,
            cap = StrokeCap.Round,
        )
    }

    if (playedX <= 0.5f) return

    val wavelength = 38.dp.toPx()
    val amplitude = min(5.dp.toPx(), size.height * 0.24f)
    val fadeDistance = wavelength * 0.72f
    val phase = wavePhase * (2f * PI.toFloat())
    val phaseOrigin = sin(phase)
    val wave = Path().apply {
        moveTo(0f, centerY)
        var x = 0f
        while (x < playedX) {
            x = min(x + 2.dp.toPx(), playedX)
            val edgeFade = min(
                1f,
                min(x / fadeDistance, (playedX - x) / fadeDistance).coerceAtLeast(0f),
            )
            val oscillation = sin((x / wavelength) * (2f * PI.toFloat()) + phase) - phaseOrigin
            val y = centerY + oscillation * amplitude * edgeFade
            lineTo(x, y)
        }
        lineTo(playedX, centerY)
    }

    drawPath(
        path = wave,
        color = playedColor,
        style = Stroke(width = playedWidth, cap = StrokeCap.Round),
    )
}
