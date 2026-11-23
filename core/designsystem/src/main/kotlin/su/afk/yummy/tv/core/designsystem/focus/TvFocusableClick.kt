package su.afk.yummy.tv.core.designsystem.focus

import su.afk.yummy.tv.core.designsystem.seasonal.seasonalFocusLights
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyMotion
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.tvFocusableClick(
    onClick: () -> Unit,
    shape: Shape = YummyShapeTokens.Compact,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onLongClick: (() -> Unit)? = null,
    focusedScale: Float = 1.04f,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    role: Role = Role.Button,
    selectedState: Boolean? = null,
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else 1f,
        animationSpec = YummyMotion.TvFocusScale,
        label = "tvFocusScale",
    )
    val borderColor by animateColorAsState(
        targetValue = if (focused) focusedBorderColor else Color.Transparent,
        animationSpec = tween(durationMillis = YummyMotion.StandardEffectsMillis),
        label = "tvFocusBorder",
    )

    return this
        .semantics {
            selectedState?.let { this.selected = it }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .seasonalFocusLights(focused, shape)
        .border(
            border = BorderStroke(width = 3.dp, color = borderColor),
            shape = shape,
        )
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick,
            role = role,
        )
}
