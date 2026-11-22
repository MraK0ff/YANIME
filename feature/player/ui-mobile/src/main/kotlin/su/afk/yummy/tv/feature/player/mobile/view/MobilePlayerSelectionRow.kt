package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.LogoBadge

@Composable
internal fun MobilePlayerSelectionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    /** Название строки акцентным цветом независимо от выбора — как в шторках скачивания. */
    accentLabel: Boolean = false,
    /** Логотип слева (студии озвучки, балансеры); [logoFallbackIcon] — когда картинки нет. Без [logoFallbackIcon] логотип не рисуется. */
    logoRes: Int? = null,
    logoFallbackIcon: ImageVector? = null,
    metaContent: @Composable ColumnScope.(contentColor: Color) -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val shape = MobilePlayerInteractiveShape
    val contentAlpha = if (enabled) 1f else 0.42f
    val background = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
    val textColor = when {
        selected -> selectedContentColor
        accentLabel -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }.copy(alpha = contentAlpha)
    val metaColor = when {
        selected -> selectedContentColor.copy(alpha = 0.82f * contentAlpha)
        accentLabel -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
        else -> textColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (logoFallbackIcon != null) {
            LogoBadge(logoRes = logoRes, size = 36.dp, fallbackIcon = logoFallbackIcon)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            metaContent(metaColor)
        }
        trailingContent?.invoke()
    }
}
