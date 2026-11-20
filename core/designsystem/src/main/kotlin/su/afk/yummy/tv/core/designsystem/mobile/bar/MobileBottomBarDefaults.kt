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

    val ExtraContentBottomPadding: Dp = 32.dp

    val contentBottomPadding: Dp
        @Composable get() = navigationBarsInset() +
            (if (LocalMobileNavigationLayout.current == MobileNavigationLayout.BottomBar) BarHeight else 0.dp) +
            ExtraContentBottomPadding

    @Composable
    private fun navigationBarsInset(): Dp =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
}
