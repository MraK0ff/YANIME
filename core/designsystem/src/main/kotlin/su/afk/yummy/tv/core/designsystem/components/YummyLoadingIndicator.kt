package su.afk.yummy.tv.core.designsystem.components

import su.afk.yummy.tv.core.model.seasonal.SeasonalTheme
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import su.afk.yummy.tv.core.designsystem.seasonal.PumpkinLoader
import su.afk.yummy.tv.core.designsystem.seasonal.rememberSeasonalTheme

/**
 * Indeterminate circular loader used across the app; change the look of every spinner here.
 *
 * Renders the shape-morphing Material 3 Expressive indicator. [strokeWidth] remains accepted for
 * source compatibility with compact call sites; the expressive indicator owns its geometry.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YummyLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") strokeWidth: Dp = Dp.Unspecified,
) {
    if (strokeWidth == Dp.Unspecified && rememberSeasonalTheme() == SeasonalTheme.HALLOWEEN) {
        PumpkinLoader(modifier)
    } else {
        LoadingIndicator(
            modifier = modifier,
            color = color.takeOrElse { MaterialTheme.colorScheme.primary },
        )
    }
}

private inline fun Color.takeOrElse(block: () -> Color): Color = if (this != Color.Unspecified) this else block()
