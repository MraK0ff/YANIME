package su.afk.yummy.tv.feature.playersetup.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingSection

@Composable
internal fun PlayerSetupMobileSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    MobileSettingSection(
        modifier = modifier,
        title = title,
        content = content,
    )
}
