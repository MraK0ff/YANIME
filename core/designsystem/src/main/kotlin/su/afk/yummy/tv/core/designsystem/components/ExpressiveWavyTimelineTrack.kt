package su.afk.yummy.tv.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

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
) {
    if (size.width <= 0f || size.height <= 0f) return

    val safeProgress = progress.coerceIn(0f, 1f)
    val safeBuffered = bufferedProgress.coerceIn(safeProgress, 1f)
    val centerY = size.height / 2f
    val playedX = size.width * safeProgress
    val bufferedX = size.width * safeBuffered
    val baseWidth = if (focused) 4.dp.toPx() else 3.dp.toPx()
    val playedWidth = if (focused) 5.dp.toPx() else 4.dp.toPx()

    // One continuous rail under all states.
    drawLine(
        color = inactiveColor,
        start = Offset(0f, centerY),
        end = Offset(size.width, centerY),
        strokeWidth = baseWidth,
        cap = StrokeCap.Round,
    )

    // Buffered media begins exactly where the played wave returns to the baseline.
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

    val halfWave = 19.dp.toPx()
    val amplitude = min(4.5.dp.toPx(), size.height * 0.22f)
    val wave = Path().apply {
        moveTo(0f, centerY)
        var x = 0f
        var waveIndex = 0

        while (x < playedX) {
            val nextX = min(x + halfWave, playedX)
            val segmentWidth = nextX - x
            if (segmentWidth <= 0.5f) break

            val direction = if (waveIndex % 2 == 0) -1f else 1f
            val isFinalSegment = nextX >= playedX - 0.5f
            val controlOneX = x + segmentWidth * 0.28f
            val controlTwoX = if (isFinalSegment) {
                nextX - segmentWidth * 0.10f
            } else {
                x + segmentWidth * 0.72f
            }
            val controlTwoY = if (isFinalSegment) centerY else centerY + direction * amplitude

            cubicTo(
                controlOneX,
                centerY + direction * amplitude,
                controlTwoX,
                controlTwoY,
                nextX,
                centerY,
            )
            x = nextX
            waveIndex++
        }
    }

    drawPath(
        path = wave,
        color = playedColor,
        style = Stroke(width = playedWidth, cap = StrokeCap.Round),
    )
}
