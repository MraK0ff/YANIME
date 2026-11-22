package su.afk.yummy.tv.core.designsystem.mobile.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
fun MobileSettingOptionRow(
    label: String,
    value: String,
    hint: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    valueColor: Color? = null,
    valueIcon: ImageVector? = null,
    valueMaxWidthFraction: Float? = 0.4f,
    valueMaxLines: Int = 2,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button)
            .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Medium),
    ) {
        // Capture BoxWithConstraints values before entering nested layout scopes.
        val safeMaxWidthFraction = valueMaxWidthFraction?.coerceIn(0f, 1f)
        val valueMaxWidth = safeMaxWidthFraction?.let { maxWidth * it }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
        ) {
            leadingIcon?.let { MobileSettingRowIcon(icon = it) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLargeEmphasized,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (hint.isNotBlank()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (valueIcon != null) {
                Icon(
                    imageVector = valueIcon,
                    contentDescription = value,
                    tint = valueColor ?: MaterialTheme.colorScheme.primary,
                )
            } else {
                val valueModifier = if (valueMaxWidth != null) {
                    Modifier.widthIn(max = valueMaxWidth)
                } else {
                    Modifier
                }
                Text(
                    text = value,
                    modifier = valueModifier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor ?: MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = valueMaxLines.coerceAtLeast(1),
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
