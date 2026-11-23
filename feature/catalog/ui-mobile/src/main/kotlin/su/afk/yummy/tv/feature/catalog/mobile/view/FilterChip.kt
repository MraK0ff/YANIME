package su.afk.yummy.tv.feature.catalog.mobile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip as MaterialFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Catalog filter chip backed by the official Material 3 component.
 *
 * Keep the catalog's compact pill geometry and tonal hierarchy, while delegating
 * interaction semantics, focus/ripple handling and selected-state behavior to Material 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val borderColor = if (selected) {
        colors.primary.copy(alpha = 0.48f)
    } else {
        colors.outlineVariant.copy(alpha = 0.72f)
    }

    MaterialFilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
        },
        trailingIcon = trailingIcon,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = true,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceContainerLow,
            labelColor = colors.onSurfaceVariant,
            iconColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
            selectedTrailingIconColor = colors.onPrimaryContainer,
        ),
        border = BorderStroke(1.dp, borderColor),
    )
}
