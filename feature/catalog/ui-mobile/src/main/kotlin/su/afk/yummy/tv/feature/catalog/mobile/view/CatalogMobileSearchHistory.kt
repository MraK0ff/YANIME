package su.afk.yummy.tv.feature.catalog.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import su.afk.yummy.tv.feature.catalog.presentation.R

/** Последние запросы чипами в горизонтальной ленте под пустым полем поиска и кнопка очистки справа. */
@Composable
internal fun CatalogMobileSearchHistory(
    queries: ImmutableList<String>,
    onQuerySelected: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Let the chip rail use the full viewport. The clear action overlays its
        // trailing edge, so LazyRow does not truncate a chip at the button boundary.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(queries, key = { it }) { query ->
                SuggestionChip(
                    onClick = { onQuerySelected(query) },
                    label = { Text(query, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
        TextButton(
            onClick = onClear,
            modifier = Modifier.align(Alignment.CenterEnd),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            Text(stringResource(R.string.search_mobile_history_clear))
        }
    }
}
