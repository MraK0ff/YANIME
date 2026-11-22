package su.afk.yummy.tv.feature.settings.mobile.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import su.afk.yummy.tv.core.designsystem.mobile.settings.MobileSettingOptionRow

@Composable
internal fun SettingsMobileOptionRow(
    label: String,
    value: String,
    hint: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Переопределяет цвет значения — например, чтобы показать реальный цвет субтитров. */
    valueColor: Color? = null,
    /** Если задана, вместо текста значения показывается значок; текст уходит в contentDescription. */
    valueIcon: ImageVector? = null,
) {
    MobileSettingOptionRow(
        label = label,
        value = value,
        hint = hint,
        onClick = onClick,
        modifier = modifier,
        valueColor = valueColor,
        valueIcon = valueIcon,
        valueMaxWidthFraction = 0.4f,
        valueMaxLines = 2,
    )
}
