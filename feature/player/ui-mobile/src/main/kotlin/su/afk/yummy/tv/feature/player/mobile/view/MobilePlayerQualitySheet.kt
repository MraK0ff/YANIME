package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.baseScreen.HideSheetWindowSystemBars
import su.afk.yummy.tv.feature.player.presentation.R

/** Выбор качества при восстановлении после ошибки — использует те же строки, что и меню плеера. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerQualitySheet(
    qualities: List<String>,
    activeQuality: String?,
    onQualitySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    BaseBottomSheet(onDismissRequest = onDismiss, scrollableContent = true) {
        HideSheetWindowSystemBars()
        MobilePlayerMenuPageHeader(
            title = stringResource(R.string.player_quality_title),
            onBack = onDismiss,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            qualities.forEach { quality ->
                MobilePlayerMenuRow(
                    label = quality,
                    trailing = MobilePlayerMenuRowTrailing.Choice(quality == activeQuality),
                    onClick = { onQualitySelected(quality) },
                )
            }
        }
    }
}
