package su.afk.yummy.tv.feature.playersetup.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingToggleRow

@Composable
internal fun PlayerSetupMobileToggleRow(
    label: String,
    icon: ImageVector,
    hint: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MobileSettingToggleRow(
        label = label,
        hint = hint,
        checked = enabled,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = icon,
    )
}
