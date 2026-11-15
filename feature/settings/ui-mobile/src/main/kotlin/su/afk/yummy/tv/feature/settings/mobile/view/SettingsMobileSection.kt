package su.afk.yummy.tv.feature.settings.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingSection

@Composable
internal fun SettingsMobileSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    MobileSettingSection(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        content = content,
    )
}
