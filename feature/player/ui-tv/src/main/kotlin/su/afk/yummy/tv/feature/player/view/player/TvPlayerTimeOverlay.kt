package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplayMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Дополнительные часы и таймкоды, которые могут оставаться видимыми после скрытия контролов. */
@Composable
internal fun TvPlayerTimeOverlay(
    settings: PlayerTimeDisplaySettings,
    displayTimeMs: Long,
    durationMs: Long,
    controlsActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val showClock = settings.systemClock.shouldShow(controlsActive)
    val showFinish = durationMs > 0L && settings.finishAt.shouldShow(controlsActive)
    val showCombinedIdle = !settings.separateTimecodes && !controlsActive && settings.elapsed == PlayerTimeDisplayMode.ALWAYS && durationMs > 0L
    val showElapsedIdle = settings.separateTimecodes && !controlsActive && settings.elapsed == PlayerTimeDisplayMode.ALWAYS && durationMs > 0L
    val showDurationIdle = settings.separateTimecodes && !controlsActive && settings.duration == PlayerTimeDisplayMode.ALWAYS && durationMs > 0L
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(showClock, showFinish) {
        if (showClock || showFinish) {
            while (true) {
                nowMs = System.currentTimeMillis()
                delay(1_000L)
            }
        }
    }

    val remainingMs = (durationMs - displayTimeMs).coerceAtLeast(0L)
    val clockFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val clock = clockFormat.format(Date(nowMs))
    val finishAt = clockFormat.format(Date(nowMs + remainingMs))

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 40.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            if (showClock) TvTimeChip(text = clock)
            if (showFinish) TvTimeChip(text = finishAt)
            if (showCombinedIdle) {
                TvTimeChip(text = "${displayTimeMs.millisToClockTime()} / ${durationMs.millisToClockTime()}")
            } else {
                if (showElapsedIdle) TvTimeChip(text = displayTimeMs.millisToClockTime())
                if (showDurationIdle) TvTimeChip(text = durationMs.millisToClockTime())
            }
        }
    }
}

@Composable
private fun TvTimeChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        maxLines = 1,
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF101318).copy(alpha = 0.68f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

private fun PlayerTimeDisplayMode.shouldShow(controlsActive: Boolean): Boolean =
    when (this) {
        PlayerTimeDisplayMode.ALWAYS -> true
        PlayerTimeDisplayMode.CONTROLS -> controlsActive
        PlayerTimeDisplayMode.NEVER -> false
    }
