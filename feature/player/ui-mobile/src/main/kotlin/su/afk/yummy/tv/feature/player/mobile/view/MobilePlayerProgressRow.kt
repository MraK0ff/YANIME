package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    isSeeking: Boolean = false,
    isPlaying: Boolean,
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
                duration = duration,
                isPlaying = isPlaying,
                selectedPlaybackSpeed = selectedSpeed,
                isSeeking = isSeeking,
                onValueChange = onSeekChange,
                onValueChangeFinished = onSeekFinished,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(32.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (qualityLabel.isNotBlank()) {
                        Row(
                            modifier = Modifier.height(40.dp).clickable(onClick = onQualityClick).padding(horizontal = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Filled.HighQuality, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
                            Text(qualityLabel, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
                        }
                    }
                    Row(
                        modifier = Modifier.height(40.dp).clickable(onClick = onSpeedClick).padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
                        Text(
                            text = if (selectedSpeed % 1f == 0f) "${selectedSpeed.toInt()}x" else "${selectedSpeed}x",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                    Box(
                        modifier = Modifier.size(40.dp).clickable(onClick = onMoreActions),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

/** Своя дорожка вместо Material Slider: тонкая линия, цвет темы, белый бегунок и буферизация. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MobilePlayerTimeline(
    progress: Float,
    bufferedProgress: Float,
    enabled: Boolean,
    duration: Long,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    isPlaying: Boolean,
    selectedPlaybackSpeed: Float,
    isSeeking: Boolean,
    modifier: Modifier = Modifier,
) {
    val playedColor = mobilePlayerTimelinePlayedColor(MaterialTheme.colorScheme.primary)
    val safeProgress = progress.coerceIn(0f, 1f)
    val safeBuffered = bufferedProgress.coerceIn(safeProgress, 1f)
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
        contentAlignment = Alignment.Center,
    ) {
        // Buffer rail remains distinct while the played segment is rendered by the
        // official Material 3 Expressive wavy indicator below.
        Canvas(Modifier.fillMaxWidth().height(32.dp)) {
            val centerY = size.height / 2f
            val trackStart = size.width * safeProgress
            val bufferEnd = size.width * safeBuffered
            drawLine(
                color = Color.White.copy(alpha = 0.20f),
                start = androidx.compose.ui.geometry.Offset(trackStart, centerY),
                end = androidx.compose.ui.geometry.Offset(size.width, centerY),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            if (bufferEnd > trackStart) {
                drawLine(
                    color = Color.White.copy(alpha = 0.34f),
                    start = androidx.compose.ui.geometry.Offset(trackStart, centerY),
                    end = androidx.compose.ui.geometry.Offset(bufferEnd, centerY),
                    strokeWidth = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
        if (isSeeking && duration > 0L) {
            Text(
                text = (duration * safeProgress).toLong().millisToClockTime(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.84f))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
        LinearWavyProgressIndicator(
            progress = { safeProgress },
            modifier = Modifier.fillMaxWidth(),
            color = playedColor,
            trackColor = Color.Transparent,
            gapSize = 0.dp,
            stopSize = 0.dp,
            amplitude = { 0.38f },
            wavelength = 28.dp,
            // Keep the wave motion perceptually tied to playback speed.
            // Use the effective player rate (including temporary speed boost) and a calmer 1x baseline.
            // The wave advances linearly with playback: half speed = half motion, double = double.
            waveSpeed = if (isPlaying) (6.dp * selectedPlaybackSpeed.coerceIn(0.25f, 3f)) else 0.dp,
        )
    }
}
