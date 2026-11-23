package su.afk.yummy.tv.feature.settings.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.focus.requestFocusUntilTimeout
import su.afk.yummy.tv.feature.settings.mobile.model.SettingsMobilePickerOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SettingsMobilePickerSheet(
    title: String,
    selectedValue: T,
    options: List<SettingsMobilePickerOption<T>>,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedOptionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(selectedValue, options) {
        requestFocusUntilTimeout(selectedOptionFocusRequester)
    }

    BaseBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        title = title,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(options, key = { it.label }) { option ->
                PickerOptionRow(
                    label = option.label,
                    hint = option.hint,
                    selected = option.value == selectedValue,
                    onClick = { onSelected(option.value) },
                    labelColor = option.labelColor,
                    icon = option.icon,
                    iconColor = option.iconColor,
                    previewColors = option.previewColors,
                    modifier = if (option.value == selectedValue) {
                        Modifier.focusRequester(selectedOptionFocusRequester)
                    } else {
                        Modifier
                    },
                )
            }
        }
    }
}

@Composable
private fun PickerOptionRow(
    label: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color? = null,
    icon: ImageVector? = null,
    iconColor: Color? = null,
    previewColors: List<Color> = emptyList(),
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow,
                shape = shape,
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when {
            previewColors.isNotEmpty() -> PalettePreview(
                colors = previewColors,
                selected = selected,
            )
            icon != null -> {
                val tint = iconColor ?: MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = labelColor ?: if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (hint.isNotBlank()) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun PalettePreview(
    colors: List<Color>,
    selected: Boolean,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colors.take(4).forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(if (index == 0) 17.dp else 13.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (selected && index == 0) {
                            Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}
