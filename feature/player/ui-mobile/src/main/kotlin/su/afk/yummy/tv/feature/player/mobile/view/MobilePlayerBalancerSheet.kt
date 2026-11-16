package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.baseScreen.HideSheetWindowSystemBars
import su.afk.yummy.tv.feature.player.presentation.R

/** Выбор балансера при восстановлении — тот же контейнер и строки, что у остальных настроек. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerBalancerSheet(
    balancerNames: List<String>,
    balancerAvailability: List<Boolean>,
    selectedIndex: Int,
    balancerExternal: List<Boolean>,
    onBalancerSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BaseBottomSheet(
        onDismissRequest = onDismiss,
        scrollableContent = true,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.player_balancer_name_prefix), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        },
    ) {
        HideSheetWindowSystemBars()
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            balancerNames.indices
                .filter { balancerAvailability.getOrElse(it) { true } }
                .forEach { index ->
                    MobilePlayerSelectionRow(
                        label = balancerNames[index],
                        selected = index == selectedIndex,
                        enabled = true,
                        accentLabel = true,
                        metaContent = { contentColor ->
                            Text(
                                text = stringResource(
                                    if (balancerExternal.getOrElse(index) { false }) {
                                        R.string.player_balancer_external_meta
                                    } else {
                                        R.string.player_balancer_meta
                                    },
                                ),
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = { onBalancerSelected(index) },
                    )
                }
        }
    }
}
