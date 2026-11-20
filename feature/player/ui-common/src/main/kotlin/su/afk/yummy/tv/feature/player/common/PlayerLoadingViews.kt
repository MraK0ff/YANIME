package su.afk.yummy.tv.feature.player.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.YummyPlayerLoadingIndicator

@Composable
fun PlayerBlackBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    )
}

@Composable
fun PlayerBufferingIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    // Keep the indicator composed while playback toggles between buffering/paused states.
    // Disposing it on a transient state change restarts the expressive loading animation.
    YummyPlayerLoadingIndicator(
        modifier = modifier.graphicsLayer { alpha = if (visible) 1f else 0f },
        size = 44.dp,
    )
}
