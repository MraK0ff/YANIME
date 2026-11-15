package su.afk.yummy.tv.core.designsystem.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

/** Кнопка онбординга: одинаково работает под пальцем и под D-pad (при фокусе заливается белым). */
@Composable
internal fun OnboardingButton(
    text: String,
    accent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val background = when {
        focused -> Color.White
        accent -> MaterialTheme.colorScheme.primary
        else -> Color.White.copy(alpha = 0.10f)
    }
    val contentColor = when {
        focused -> Color.Black
        accent -> MaterialTheme.colorScheme.onPrimary
        else -> Color.White
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = contentColor,
        modifier = modifier
            .clip(YummyShapeTokens.Control)
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                onClick = onClick,
                role = Role.Button,
            )
            .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Medium)
            .heightIn(min = YummySpacing.TouchTarget),
    )
}
