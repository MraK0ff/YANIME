package su.afk.yummy.tv.feature.details.mobile.details.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
internal fun DetailsChip(
    label: String,
    icon: ImageVector? = null,
    textColor: Color? = null,
    backgroundColor: Color? = null,
) {
    Row(
        modifier = Modifier
            .background(
                color = backgroundColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f),
                shape = YummyShapeTokens.Compact,
            )
            .padding(horizontal = YummySpacing.Small, vertical = YummySpacing.XSmall),
        horizontalArrangement = Arrangement.spacedBy(YummySpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                modifier = Modifier.size(YummySpacing.Medium),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmallEmphasized,
            color = textColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            maxLines = 1,
        )
    }
}
