package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime

@Composable
internal fun MobilePlayerProgressRow(
    displayTime: Long,
    duration: Long,
    seekProgress: Float,
    bufferedProgress: Float,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    videoWidthFraction: Float = 1f,
    videoBottomInset: Dp = 0.dp,
    timeDisplaySettings: PlayerTimeDisplaySettings = PlayerTimeDisplaySettings(),
    showRemainingTime: Boolean = false,
    onTimeCodeClick: () -> Unit = {},
    dubbingLabel: String = "",
    balancerLabel: String = "",
    onTrackSettings: () -> Unit = {},
    qualityLabel: String = "",
    selectedSpeed: Float = 1f,
    onQualityClick: () -> Unit = {},
    onSpeedClick: () -> Unit = {},
    onMoreActions: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Symmetric layout: timecode at the top, timeline below it, with matching outer spacing.
    Box(
        modifier = modifier
            .fillMaxWidth(videoWidthFraction.coerceIn(0.1f, 1f))
            .height(88.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().offset(y = -videoBottomInset).padding(horizontal = 20.dp),
        ) {
            MobilePlayerTimeline(
                progress = seekProgress.coerceIn(0f, 1f),
                bufferedProgress = bufferedProgress.coerceIn(seekProgress.coerceIn(0f, 1f), 1f),
                enabled = duration > 0L,
                onValueChange = onSeekChange,
                onValueChangeFinished = onSeekFinished,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(32.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(32.dp),
            ) {
                val remaining = (duration - displayTime).coerceAtLeast(0L)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(32.dp)
                        .clickable(role = Role.Button, onClick = onTimeCodeClick)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (showRemainingTime) "-" + remaining.millisToClockTime().padStart(5, '0') else displayTime.millisToClockTime().padStart(5, '0'),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                    Text("/", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.58f))
                    Text(
                        text = duration.millisToClockTime().padStart(5, '0'),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.62f),
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (qualityLabel.isNotBlank()) {
                        Text(
                            text = qualityLabel,
                            modifier = Modifier.clickable(onClick = onQualityClick),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = if (selectedSpeed % 1f == 0f) "${selectedSpeed.toInt()}x" else "${selectedSpeed}x",
                        modifier = Modifier.clickable(onClick = onSpeedClick),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                    )
                    Icon(
                        imageVector = Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).clickable(onClick = onMoreActions),
                    )
                }
            }
        }
    }
}

/** Своя дорожка вместо Material Slider: тонкая линия, цвет темы, белый бегунок и буферизация. */
@Composable
private fun MobilePlayerTimeline(
    progress: Float,
    bufferedProgress: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playedColor = mobilePlayerTimelinePlayedColor(MaterialTheme.colorScheme.primary)
    val bufferedColor = Color.White.copy(alpha = 0.34f)
    // Keep the inactive rail quiet so the played segment remains the visual anchor.
    val unplayedColor = Color.White.copy(alpha = 0.18f)
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(32.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(safeProgress, 0f..1f)
                if (enabled) setProgress { target ->
                    onValueChange(target.coerceIn(0f, 1f))
                    onValueChangeFinished()
                    true
                }
            }
            .pointerInput(enabled, onValueChange) {
                if (enabled) {
                    detectTapGestures { position ->
                        val fraction = (position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onValueChange(fraction)
                        onValueChangeFinished()
                    }
                }
            }
            .pointerInput(enabled, onValueChange, onValueChangeFinished) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { position ->
                            onValueChange((position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            onValueChange((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onDragEnd = onValueChangeFinished,
                        onDragCancel = onValueChangeFinished,
                    )
                }
            },
    ) {
        Canvas(Modifier.fillMaxWidth().height(32.dp)) {
            val centerY = size.height / 2f
            val stroke = 3.dp.toPx()
            val radius = stroke / 2f
            val endX = size.width
            val playedX = endX * safeProgress
            val bufferedX = endX * bufferedProgress.coerceIn(safeProgress, 1f)
            drawRoundRect(
                color = unplayedColor,
                topLeft = Offset(0f, centerY - radius),
                size = Size(endX, stroke),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            )
            if (bufferedX > playedX) {
                drawRoundRect(
                    color = bufferedColor,
                    topLeft = Offset(playedX, centerY - radius),
                    size = Size(bufferedX - playedX, stroke),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
            }
            if (playedX > 0f) {
                drawRoundRect(
                    color = playedColor,
                    topLeft = Offset(0f, centerY - radius),
                    size = Size(playedX, stroke),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
            }
            if (enabled) {
                drawCircle(
                    color = playedColor,
                    radius = 5.dp.toPx(),
                    center = Offset(playedX.coerceIn(0f, endX), centerY),
                )
            }
        }
    }
}
