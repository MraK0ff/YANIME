package su.afk.yummy.tv.feature.settings.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingToggleRow

@Composable
internal fun SettingsMobileToggleRow(
    label: String,
    hint: String? = null,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MobileSettingToggleRow(
        label = label,
        hint = hint.orEmpty(),
        checked = enabled,
        onClick = onClick,
        modifier = modifier,
    )
}
