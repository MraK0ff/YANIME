package su.afk.yummy.tv.core.designsystem.mobile.bar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MobileBottomBarDefaults {

    /** Высота бара с постоянно видимыми подписями, согласованная с нижней навигацией Material 3. */
    val BarHeight: Dp = 80.dp

    val ExtraContentBottomPadding: Dp = 16.dp

    /**
     * Отступ снизу для скроллящегося контента корневых экранов табов. Нижний бар сам занимает
     * место вместе с инсетом системной навигации, поэтому над ним нужен только зазор; при рейке
     * или скрытой навигации контент доходит до края окна и сам уходит от системного инсета.
     */
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
