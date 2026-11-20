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

    val ExtraContentBottomPadding: Dp = 16.dp

    val contentBottomPadding: Dp
        @Composable get() = when (LocalMobileNavigationLayout.current) {
            MobileNavigationLayout.BottomBar -> ExtraContentBottomPadding
            MobileNavigationLayout.Rail,
            MobileNavigationLayout.Hidden,
                -> navigationBarsInset() + ExtraContentBottomPadding
        }

    @Composable
    private fun navigationBarsInset(): Dp =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
}
