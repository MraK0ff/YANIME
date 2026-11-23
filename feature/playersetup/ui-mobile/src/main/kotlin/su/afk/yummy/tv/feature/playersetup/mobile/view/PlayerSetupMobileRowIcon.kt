package su.afk.yummy.tv.feature.playersetup.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingRowIcon

@Composable
internal fun PlayerSetupMobileRowIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    MobileSettingRowIcon(icon = icon, modifier = modifier)
}
