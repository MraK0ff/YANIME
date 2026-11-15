package su.afk.yummy.tv.feature.account.mobile.view

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
internal fun AccountMobileProfileMetaChip(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier,
) {
    if (value.isBlank()) return
    Row(
        modifier = modifier
            .clip(YummyShapeTokens.Pill)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
            .padding(horizontal = YummySpacing.Medium, vertical = YummySpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(YummySpacing.XSmall),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(YummySpacing.Large - 2.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
