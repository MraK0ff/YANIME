package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplayMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.feature.player.common.model.StepSeekDirection
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime
import su.afk.yummy.tv.feature.player.common.utils.openingRange

@Composable
internal fun TvPlayerProgressRow(
    wantsPlay: Boolean,
    displayTime: Long,
    duration: Long,
    timeDisplaySettings: PlayerTimeDisplaySettings,
    isSeeking: Boolean,
    seekProgress: Float,
    bufferedProgress: Float,
    currentPosition: Long,
    openingStartMs: Long?,
    openingEndMs: Long?,
    playFocusRequester: FocusRequester,
    playUpFocusRequester: FocusRequester?,
    progressFocusRequester: FocusRequester,
    progressDownFocusRequester: FocusRequester,
    onPlayPause: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onStepSeek: (StepSeekDirection) -> Unit,
    onSeekFinished: () -> Unit,
    onInteraction: () -> Unit,
) {
    var sliderJustFocused by remember { mutableStateOf(false) }
    var sliderFocused by remember { mutableStateOf(false) }

    val showElapsed = timeDisplaySettings.elapsed != PlayerTimeDisplayMode.NEVER
    val showDuration = timeDisplaySettings.duration != PlayerTimeDisplayMode.NEVER
    val elapsedText = displayTime.millisToClockTime()
    val durationText = duration.millisToClockTime()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TvControlButton(
            onClick = { onPlayPause(); onInteraction() },
            onFocused = onInteraction,
            focusRequester = playFocusRequester,
            modifier = Modifier.focusProperties {
                if (playUpFocusRequester != null) up = playUpFocusRequester
                down = progressFocusRequester
            },
            primary = true,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        ) { color ->
            Icon(
                imageVector = if (wantsPlay) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp),
            )
        }
        if (showElapsed && !timeDisplaySettings.separateTimecodes) {
            Text(
                text = if (showDuration) "$elapsedText / $durationText" else elapsedText,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        } else if (showElapsed) {
            Text(
                text = elapsedText,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        } else if (showDuration && !timeDisplaySettings.separateTimecodes) {
            Text(
                text = durationText,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
        PlayerBufferedSlider(
            value = when {
                isSeeking -> seekProgress
                duration > 0 -> currentPosition.toFloat() / duration
                else -> 0f
            },
            bufferedProgress = bufferedProgress,
            openingRange = openingRange(openingStartMs, openingEndMs, duration),
            onValueChange = { v ->
                if (sliderJustFocused) {
                    sliderJustFocused = false
                } else {
                    onSeekChange(v)
                    onInteraction()
                }
            },
            onValueChangeFinished = onSeekFinished,
            focused = sliderFocused,
            modifier = Modifier
                .weight(1f)
                .focusRequester(progressFocusRequester)
                .focusProperties {
                    up = playFocusRequester
                    down = progressDownFocusRequester
                }
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.DirectionLeft -> {
                                onStepSeek(StepSeekDirection.Backward)
                                onInteraction()
                                return@onPreviewKeyEvent true
                            }
                            Key.DirectionRight -> {
                                onStepSeek(StepSeekDirection.Forward)
                                onInteraction()
                                return@onPreviewKeyEvent true
                            }
                            else -> Unit
                        }
                    }
                    when (event.key) {
                        Key.DirectionUp -> {
                            when (event.type) {
                                KeyEventType.KeyDown -> playFocusRequester.requestFocus()
                                KeyEventType.KeyUp -> Unit
                                else -> return@onPreviewKeyEvent false
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            when (event.type) {
                                KeyEventType.KeyDown -> progressDownFocusRequester.requestFocus()
                                KeyEventType.KeyUp -> Unit
                                else -> return@onPreviewKeyEvent false
                            }
                            true
                        }
                        else -> false
                    }
                }
                .onFocusChanged {
                    sliderFocused = it.isFocused
                    if (it.isFocused) sliderJustFocused = true
                },
        )
        if (showDuration && timeDisplaySettings.separateTimecodes) {
            Text(
                text = durationText,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlayerBufferedSlider(
    value: Float,
    bufferedProgress: Float,
    openingRange: ClosedFloatingPointRange<Float>?,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    focused: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeProgress = value.coerceIn(0f, 1f)
    val buffered = bufferedProgress.coerceIn(activeProgress, 1f)
    val focusedColor = MaterialTheme.colorScheme.primary
    val sliderColor by animateColorAsState(
        targetValue = focusedColor,
        animationSpec = tween(TV_PLAYER_FOCUS_ANIMATION_DURATION_MS),
        label = "tvPlayerProgressColor",
    )
    val bufferedColor by animateColorAsState(
        targetValue = focusedColor.copy(alpha = if (focused) 0.62f else 0.42f),
        animationSpec = tween(TV_PLAYER_FOCUS_ANIMATION_DURATION_MS),
        label = "tvPlayerBufferedProgressColor",
    )
    val inactiveTrackColor by animateColorAsState(
        targetValue = Color.White.copy(alpha = if (focused) 0.26f else 0.20f),
        animationSpec = tween(TV_PLAYER_FOCUS_ANIMATION_DURATION_MS),
        label = "tvPlayerInactiveProgressColor",
    )
    val colors = SliderDefaults.colors(
        thumbColor = sliderColor,
        activeTrackColor = sliderColor,
        inactiveTrackColor = inactiveTrackColor,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    val sliderState = remember { SliderState(value = activeProgress) }
    sliderState.value = activeProgress
    Slider(
        state = sliderState,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier,
        colors = colors,
        thumb = {
            Canvas(Modifier.size(if (focused) 14.dp else 10.dp)) {
                drawCircle(
                    color = sliderColor,
                    radius = size.minDimension / 2f,
                )
            }
        },
        track = { state ->
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(32.dp),
            ) {
                val centerY = size.height / 2f
                val trackHeight = if (focused) 4.dp.toPx() else 3.dp.toPx()
                val radius = trackHeight / 2f
                val end = size.width
                val played = end * state.value.coerceIn(0f, 1f)
                val bufferedEnd = end * buffered.coerceIn(state.value.coerceIn(0f, 1f), 1f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    topLeft = Offset(0f, centerY - radius),
                    size = Size(end, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                if (bufferedEnd > played) drawRoundRect(
                    color = Color.White.copy(alpha = 0.34f),
                    topLeft = Offset(played, centerY - radius),
                    size = Size(bufferedEnd - played, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                if (played > 0f) drawRoundRect(
                    color = sliderColor,
                    topLeft = Offset(0f, centerY - radius),
                    size = Size(played, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                openingRange?.let { range ->
                    drawRoundRect(
                        color = focusedColor.copy(alpha = 0.95f),
                        topLeft = Offset(end * range.start.coerceIn(0f, 1f), centerY - radius),
                        size = Size(end * (range.endInclusive - range.start).coerceAtLeast(0f), trackHeight),
                        cornerRadius = CornerRadius(radius, radius),
                    )
                }
            }
        },
    )
}
