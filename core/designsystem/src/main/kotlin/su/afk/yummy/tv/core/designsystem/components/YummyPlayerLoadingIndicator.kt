package su.afk.yummy.tv.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Единый выразительный Material 3 индикатор для буферизации и кнопки Play/Pause. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YummyPlayerLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = Color.White,
    @Suppress("UNUSED_PARAMETER") strokeWidth: Dp = 2.dp,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        LoadingIndicator(
            modifier = Modifier.fillMaxSize(),
            color = color,
        )
    }
}
