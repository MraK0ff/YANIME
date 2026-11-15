package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.skeleton.rememberAnimationsEnabled
import su.afk.yummy.tv.core.designsystem.theme.YummyMotion
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.feature.player.common.view.autoSkipProgressFill

@Composable
internal fun TvControlButton(
    onClick: () -> Unit,
    onFocused: () -> Unit = {},
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    fillProgress: Float? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    content: @Composable (textColor: Color) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var focused by remember { mutableStateOf(false) }
    val animationsEnabled = rememberAnimationsEnabled()
    val colorAnimationSpec = if (animationsEnabled) tween<Color>(YummyMotion.FastEffectsMillis) else snap()
    val shape = YummyShapeTokens.Control
    val colors = MaterialTheme.colorScheme
    val bgColor by animateColorAsState(
        targetValue = when {
            focused -> colors.primary
            // Тёмная подложка: на светлых кадрах (заставки) прозрачная кнопка теряется.
            primary -> Color.Black.copy(alpha = 0.62f)
            else -> Color.Black.copy(alpha = 0.5f)
        },
        animationSpec = colorAnimationSpec,
        label = "tvControlButtonBackground",
    )
    val textColor by animateColorAsState(
        targetValue = if (focused) colors.onPrimary else Color.White,
        animationSpec = colorAnimationSpec,
        label = "tvControlButtonContent",
    )
    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.primary else Color.White.copy(alpha = 0.6f),
        animationSpec = colorAnimationSpec,
        label = "tvControlButtonBorder",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .tvPlayerFocusScale(focused)
            .border(1.5.dp, borderColor, shape)
            .background(bgColor, shape)
            .autoSkipProgressFill(fillProgress, textColor.copy(alpha = 0.22f), 8.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(contentPadding),
    ) {
        content(textColor)
    }
}
