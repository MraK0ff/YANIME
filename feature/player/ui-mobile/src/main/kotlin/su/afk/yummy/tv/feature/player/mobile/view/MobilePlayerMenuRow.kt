package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Режим правой части строки меню настроек. */
internal sealed interface MobilePlayerMenuRowTrailing {
    data class Navigate(val value: String? = null) : MobilePlayerMenuRowTrailing
    data class Toggle(val checked: Boolean) : MobilePlayerMenuRowTrailing
    data class Choice(val selected: Boolean) : MobilePlayerMenuRowTrailing
    data object None : MobilePlayerMenuRowTrailing
}

/** Строка настроек в актуальном стиле плеера: компактная иконка, подпись и состояние справа. */
@Composable
internal fun MobilePlayerMenuRow(
    label: String,
    trailing: MobilePlayerMenuRowTrailing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = if (trailing is MobilePlayerMenuRowTrailing.Toggle) Role.Switch else Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .background(
                color = if (trailing is MobilePlayerMenuRowTrailing.Choice && trailing.selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            when {
                icon != null -> Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                trailing is MobilePlayerMenuRowTrailing.Choice && trailing.selected -> Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (trailing is MobilePlayerMenuRowTrailing.Choice && trailing.selected) FontWeight.SemiBold else null,
                color = if (trailing is MobilePlayerMenuRowTrailing.Choice && trailing.selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else labelColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when (trailing) {
            is MobilePlayerMenuRowTrailing.Navigate -> {
                if (trailing.value != null) {
                    Text(
                        text = trailing.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp),
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is MobilePlayerMenuRowTrailing.Toggle -> Switch(checked = trailing.checked, onCheckedChange = null)
            is MobilePlayerMenuRowTrailing.Choice -> if (icon != null && trailing.selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            MobilePlayerMenuRowTrailing.None -> Unit
        }
    }
}
