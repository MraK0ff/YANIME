package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.feature.player.presentation.R

/** Заголовок вложенной страницы: текст слева, без дублирующей стрелки справа. */
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun MobilePlayerMenuPageHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(bottom = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = if (value == null) title else "$title • $value",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** Заголовок шторки: название слева и кнопка сворачивания по центру. */
@Composable
internal fun MobilePlayerPanelDragTitle(
    title: String,
    onCollapse: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp),
        )
        if (onCollapse != null) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier.align(Alignment.Center),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.player_side_panel_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
