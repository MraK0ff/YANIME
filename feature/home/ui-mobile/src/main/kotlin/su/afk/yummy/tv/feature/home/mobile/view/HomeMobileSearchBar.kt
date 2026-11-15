package su.afk.yummy.tv.feature.home.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.feature.home.presentation.R

/**
 * Строка поиска вверху Главной. Сама ввод не принимает: по нажатию открывается Каталог
 * с фокусом в настоящем поле, поэтому внешне повторяет его поле запроса.
 */
@Composable
internal fun HomeMobileSearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = YummyShapeTokens.Control
    val label = stringResource(R.string.home_mobile_search_placeholder)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = YummySpacing.TouchTarget + YummySpacing.Small)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clickablePointer()
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = YummySpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
