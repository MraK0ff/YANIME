package su.afk.yummy.tv.feature.main.mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.GlobalToastOverlay
import su.afk.yummy.tv.core.designsystem.mobile.bar.LocalMobileBottomBarUpFocusRequester
import su.afk.yummy.tv.core.designsystem.mobile.bar.LocalMobileNavigationLayout
import su.afk.yummy.tv.core.designsystem.mobile.bar.MobileBottomBarDefaults
import su.afk.yummy.tv.core.designsystem.mobile.bar.MobileNavigationLayout
import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalOverlay
import su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem

/**
 * Root mobile layout. The page always draws edge-to-edge; navigation is an overlay, not a
 * reserved scaffold row, so the page remains visible through the transparent area around capsules.
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
    val navigationSurface = MaterialTheme.colorScheme.surfaceContainerHigh

    LaunchedEffect(selectedDestination) {
        headerVisible.value = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalMobileNavigationLayout provides layout) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isRail) Modifier.padding(start = 80.dp) else Modifier),
                ) {
                    val barInsets = if (layout == MobileNavigationLayout.BottomBar) {
                        WindowInsets.navigationBars.add(
                            WindowInsets(bottom = MobileBottomBarDefaults.BarHeight),
                        )
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
                            content()
                            if (showSeasonalOverlay) SeasonalOverlay()
                            GlobalToastOverlay(
                                text = toastMessage,
                                modifier = Modifier.padding(
                                    bottom = MobileBottomBarDefaults.contentBottomPadding,
                                ),
                            )
                            AnimatedVisibility(
                                visible = showHeader && headerVisible.value,
                                modifier = Modifier.align(Alignment.TopCenter),
                                enter = fadeIn() + slideInVertically { -it / 2 },
                                exit = fadeOut() + slideOutVertically { -it / 2 },
                            ) {
                                MobileMainHeader(
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = Color.Transparent,
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
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = Color.Transparent,
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
                                    .fillMaxWidth()
                                    // Header already owns the status-bar inset. Prevent nested
                                    // page Scaffolds from inserting a second empty strip below it.
                                    .consumeWindowInsets(WindowInsets.statusBars),
                            ) {
                                content()
                                if (showSeasonalOverlay) SeasonalOverlay()
                                GlobalToastOverlay(
                                    text = toastMessage,
                                    modifier = Modifier.padding(
                                        bottom = MobileBottomBarDefaults.contentBottomPadding,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                modifier = Modifier.align(
                    if (isRail) Alignment.CenterStart else Alignment.BottomCenter,
                ),
                enter = if (isRail) {
                    fadeIn() + slideInHorizontally { -it / 3 }
                } else {
                    fadeIn() + slideInVertically { it / 2 }
                },
                exit = if (isRail) {
                    fadeOut() + slideOutHorizontally { -it / 3 }
                } else {
                    fadeOut() + slideOutVertically { it / 2 }
                },
            ) {
                if (isRail) {
                    MobileMainNavigationRail(
                        selectedDestination = selectedDestination,
                        menuItems = menuItems,
                        containerColor = navigationSurface,
                        onDestinationSelected = onDestinationSelected,
                    )
                } else {
                    MobileMainNavigationBar(
                        selectedDestination = selectedDestination,
                        menuItems = menuItems,
                        containerColor = navigationSurface,
                        upFocusRequester = bottomBarUpFocusRequester,
                        classicStyle = classicBottomBar,
                        searchFabEnabled = searchFabEnabled,
                        onSearchSelected = onSearchSelected,
                        onDestinationSelected = onDestinationSelected,
                    )
                }
            }
        }
    }
}
