package su.afk.yummy.tv.feature.playersetup.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingOptionRow

/** Строка выбора значения: название, подсказка и текущее значение; нажатие открывает шторку. */
@Composable
internal fun PlayerSetupMobileOptionRow(
    label: String,
    icon: ImageVector,
    value: String,
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /** Если задана, вместо текста значения показывается значок; текст уходит в contentDescription. */
    valueIcon: ImageVector? = null,
) {
    MobileSettingOptionRow(
        label = label,
        value = value,
        hint = hint,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = icon,
        valueIcon = valueIcon,
        valueMaxWidthFraction = 0.4f,
        valueMaxLines = 1,
    )
}
