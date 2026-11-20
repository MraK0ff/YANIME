package su.afk.yummy.tv.feature.main.mobile.view

import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalOverlay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.GlobalToastOverlay
import su.afk.yummy.tv.core.designsystem.mobile.bar.LocalMobileBottomBarUpFocusRequester
import su.afk.yummy.tv.core.designsystem.mobile.bar.LocalMobileNavigationLayout
import su.afk.yummy.tv.core.designsystem.mobile.bar.MobileBottomBarDefaults
import su.afk.yummy.tv.core.designsystem.mobile.bar.MobileNavigationLayout
import su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem

/**
 * Корневой каркас мобильного UI: на компактной ширине — плавающая нижняя навигация,
 * на широком окне — боковая рейка. Глобальная шапка показывается только на корнях основных
 * разделов и не вторгается в профиль, детали или полноэкранные сценарии.
 */
@Composable
internal fun <T> MobileMainScaffold(
    selectedDestination: T,
    menuItems: List<MobileMenuItem<T>>,
    showBottomBar: Boolean,
    showRail: Boolean,
    showHeader: Boolean,
    overlayHeader: Boolean = false,
    classicBottomBar: Boolean = false,
    searchFabEnabled: Boolean = false,
    headerAvatarUrl: String,
    unreadNotificationsCount: Int,
    onSearchSelected: () -> Unit,
    onNotificationsSelected: () -> Unit,
    onSettingsSelected: () -> Unit,
    onProfileSelected: () -> Unit,
    onDestinationSelected: (T) -> Unit,
    toastMessage: String?,
    showSeasonalOverlay: Boolean = true,
    content: @Composable () -> Unit,
) {
    val bottomBarUpFocusRequester = LocalMobileBottomBarUpFocusRequester.current
    val headerVisible = remember { mutableStateOf(true) }
    val headerScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                when {
                    consumed.y < -1f -> headerVisible.value = false
                    consumed.y > 1f -> headerVisible.value = true
                }
                return Offset.Zero
            }
        }
    }
    val suiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())
    val isRail = suiteType != NavigationSuiteType.NavigationBar &&
        suiteType != NavigationSuiteType.ShortNavigationBarCompact &&
        suiteType != NavigationSuiteType.ShortNavigationBarMedium
    val isVisible = if (isRail) showRail else showBottomBar
    val layout = when {
        !isVisible -> MobileNavigationLayout.Hidden
        isRail -> MobileNavigationLayout.Rail
        else -> MobileNavigationLayout.BottomBar
    }

    val suiteState = rememberNavigationSuiteScaffoldState()
    LaunchedEffect(selectedDestination) {
        headerVisible.value = true
    }
    LaunchedEffect(isVisible) {
        if (isVisible) suiteState.show() else suiteState.hide()
    }

    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
    NavigationSuiteScaffoldLayout(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navigationSuiteType = if (isRail) NavigationSuiteType.NavigationRail else suiteType,
        state = suiteState,
        navigationSuite = {
            if (isRail) {
                MobileMainNavigationRail(
                    selectedDestination = selectedDestination,
                    menuItems = menuItems,
                    containerColor = surface,
                    onDestinationSelected = onDestinationSelected,
                )
            } else {
                MobileMainNavigationBar(
                    selectedDestination = selectedDestination,
                    menuItems = menuItems,
                    containerColor = surface,
                    upFocusRequester = bottomBarUpFocusRequester,
                    classicStyle = classicBottomBar,
                    searchFabEnabled = searchFabEnabled,
                    onSearchSelected = onSearchSelected,
                    onDestinationSelected = onDestinationSelected,
                )
            }
        },
    ) {
        CompositionLocalProvider(LocalMobileNavigationLayout provides layout) {
            val barInsets = if (layout == MobileNavigationLayout.BottomBar) {
                WindowInsets.navigationBars.add(WindowInsets(bottom = MobileBottomBarDefaults.BarHeight))
            } else {
                WindowInsets(0.dp)
            }
            if (overlayHeader) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(headerScrollConnection)
                        .consumeWindowInsets(barInsets)
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        content()
                        if (showSeasonalOverlay) SeasonalOverlay()
                        GlobalToastOverlay(
                            text = toastMessage,
                            modifier = Modifier.padding(bottom = MobileBottomBarDefaults.contentBottomPadding),
                        )
                    }
                    AnimatedVisibility(
                        visible = showHeader && headerVisible.value,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter),
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit = fadeOut() + slideOutVertically { -it / 2 },
                    ) {
                        MobileMainHeader(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                            showSearchBar = !searchFabEnabled,
                            avatarUrl = headerAvatarUrl,
                            unreadNotificationsCount = unreadNotificationsCount,
                            showSearchBar = !searchFabEnabled,
                            onSearchSelected = onSearchSelected,
                            onNotificationsSelected = onNotificationsSelected,
                            onSettingsSelected = onSettingsSelected,
                            onProfileSelected = onProfileSelected,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(headerScrollConnection)
                        .consumeWindowInsets(barInsets)
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    AnimatedVisibility(
                        visible = showHeader && headerVisible.value,
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit = fadeOut() + slideOutVertically { -it / 2 },
                    ) {
                        MobileMainHeader(
                            avatarUrl = headerAvatarUrl,
                            unreadNotificationsCount = unreadNotificationsCount,
                            showSearchBar = !searchFabEnabled,
                            onSearchSelected = onSearchSelected,
                            onNotificationsSelected = onNotificationsSelected,
                            onSettingsSelected = onSettingsSelected,
                            onProfileSelected = onProfileSelected,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        content()
                        if (showSeasonalOverlay) SeasonalOverlay()
                        GlobalToastOverlay(
                            text = toastMessage,
                            modifier = Modifier.padding(bottom = MobileBottomBarDefaults.contentBottomPadding),
                        )
                    }
                }
            }
        }
    }
    }
}
