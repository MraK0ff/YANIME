package su.afk.yummy.tv.feature.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import su.afk.yummy.tv.feature.settings.common.utils.color
import su.afk.yummy.tv.feature.settings.common.utils.hint
import su.afk.yummy.tv.feature.settings.common.utils.label

@Composable
internal fun QualityRow(
    label: String,
    hint: String = "",
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Переопределяет цвет текста подписи — например, чтобы показать реальный цвет субтитров. */
    labelColor: Color? = null,
    /** Значок варианта; без него строка остаётся только с подписью. Тонируется [labelColor], если он задан. */
    icon: ImageVector? = null,
    previewColors: List<Color> = emptyList(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(18.dp)

    val bgColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        focused -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val borderColor = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent
    val effectiveLabelColor = labelColor ?: if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(shape)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .background(bgColor, shape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RadioDot(selected = selected, focused = focused)

        if (previewColors.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                previewColors.take(4).forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(if (index == 0) 24.dp else 18.dp)
                            .background(color, CircleShape)
                            .then(
                                if (selected && index == 0) {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)
                                } else Modifier,
                            ),
                    )
                }
            }
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = labelColor ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = effectiveLabelColor,
            )
            if (hint.isNotBlank()) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean, focused: Boolean) {
    val ringColor = when {
        selected -> MaterialTheme.colorScheme.primary
        focused -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(width = 2.dp, color = ringColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}
