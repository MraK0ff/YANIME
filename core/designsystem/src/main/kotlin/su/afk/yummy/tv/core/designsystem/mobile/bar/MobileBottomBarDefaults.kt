package su.afk.yummy.tv.core.designsystem.mobile.bar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MobileBottomBarDefaults {

    /** Полная высота плавающей панели с вертикальными полями; сама капсула имеет высоту 64 dp. */
    val BarHeight: Dp = 72.dp

    val ExtraContentBottomPadding: Dp = 12.dp

    val contentBottomPadding: Dp
        @Composable get() = navigationBarsInset() +
            (if (LocalMobileNavigationLayout.current == MobileNavigationLayout.BottomBar) BarHeight else 0.dp) +
            ExtraContentBottomPadding

    /**
     * То же, но для корня таба, который остаётся в композиции под экраном поверх него (рецензии
     * с главной). Бар скрывается и возвращается вместе с экраном, и вьюпорт списка меняется на
     * высоту бара и системного инсета. Если отступ при этом меняется меньше, LazyList зажимает
     * сохранённую прокрутку, и после возврата нижние кнопки уходят под бар. Поэтому при скрытом
     * баре добавляем отступ той же высоты: предел прокрутки не меняется.
     */
    val rootContentBottomPadding: Dp
        @Composable get() = when (LocalMobileNavigationLayout.current) {
            MobileNavigationLayout.BottomBar -> ExtraContentBottomPadding
            MobileNavigationLayout.Rail -> navigationBarsInset() + ExtraContentBottomPadding
            MobileNavigationLayout.Hidden -> navigationBarsInset() + BarHeight + ExtraContentBottomPadding
        }

    @Composable
    private fun navigationBarsInset(): Dp =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
}
