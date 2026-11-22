package su.afk.yummy.tv.feature.main.mobile

import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalPrompt
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import su.afk.yummy.tv.core.common.coroutines.AppClock
import su.afk.yummy.tv.core.designsystem.baseScreen.ScreenNavigator
import su.afk.yummy.tv.core.designsystem.components.rememberGlobalToastState
import su.afk.yummy.tv.core.designsystem.dialog.ChoiceDialogHost
import su.afk.yummy.tv.core.designsystem.dialog.ConfirmDialogHost
import su.afk.yummy.tv.core.designsystem.locals.LocalIsOffline
import su.afk.yummy.tv.core.designsystem.locals.LocalLibraryListStatuses
import su.afk.yummy.tv.core.designsystem.locals.LocalLibraryFavoriteAnimeIds
import su.afk.yummy.tv.core.designsystem.locals.LocalLibraryUserRatings
import su.afk.yummy.tv.core.designsystem.locals.LocalNowMillis
import su.afk.yummy.tv.core.designsystem.locals.LocalPosterCardSize
import su.afk.yummy.tv.core.designsystem.onboarding.LocalOnboardingController
import su.afk.yummy.tv.core.designsystem.onboarding.rememberOnboardingController
import su.afk.yummy.tv.core.designsystem.mobile.bar.LocalMobileBottomBarUpFocusRequester
import su.afk.yummy.tv.core.designsystem.seasonal.LocalSeasonalDecorationsEnabled
import su.afk.yummy.tv.core.designsystem.theme.YummyTvTheme
import su.afk.yummy.tv.core.navigation.host.AppNavHost
import su.afk.yummy.tv.core.navigation.manager.BackStackHost
import su.afk.yummy.tv.core.navigation.manager.INavigationManager
import su.afk.yummy.tv.core.navigation.root.RootTab
import su.afk.yummy.tv.core.navigation.scene.FullscreenDestination
import su.afk.yummy.tv.feature.catalog.ICatalogSearchOpener
import su.afk.yummy.tv.feature.account.navigator.AccountNotificationsDestination
import su.afk.yummy.tv.feature.settings.navigator.SettingsDestination
import su.afk.yummy.tv.feature.main.presentation.R
import su.afk.yummy.tv.feature.main.MainState
import su.afk.yummy.tv.feature.main.MainViewModel
import su.afk.yummy.tv.feature.main.api.MainGraph
import su.afk.yummy.tv.feature.main.mobile.model.mobileMenuItems
import su.afk.yummy.tv.feature.main.mobile.navigation.MobileNavigationHolder
import su.afk.yummy.tv.feature.main.mobile.navigation.mobileNavPopTransitionSpec
import su.afk.yummy.tv.feature.main.mobile.navigation.mobileNavTransitionSpec
import su.afk.yummy.tv.feature.main.mobile.navigation.rememberMobileListDetailSceneStrategy
import su.afk.yummy.tv.feature.main.mobile.view.MobileMainScaffold
import su.afk.yummy.tv.feature.update.navigator.UpdateDestination
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileMainGraph @Inject internal constructor(
    private val appClock: AppClock,
    private val navManager: INavigationManager,
    private val backStackHost: BackStackHost,
    private val navigationHolder: MobileNavigationHolder,
    private val catalogSearchOpener: ICatalogSearchOpener,
) : MainGraph {

    @Composable
    override fun MainGraph() {
        val viewModel: MainViewModel = hiltViewModel()
        val currentDestination = navManager.backStack.lastOrNull()
        val atTabRoot = navManager.appBackStack.isEmpty() && navManager.backStack.size <= 1
        val isRequiredUpdateDestination =
            currentDestination is UpdateDestination && currentDestination.required
        val isFullscreenDestination = currentDestination is FullscreenDestination
        val isProfileRoot = atTabRoot && navManager.currentRoot == RootTab.ACCOUNT
        val showRail = !isRequiredUpdateDestination && !isFullscreenDestination

        val profileTabLabel = stringResource(R.string.main_mobile_tab_profile)
        ScreenNavigator(viewModel) { state, effect, onEvent ->
            if (!state.appearanceLoaded) return@ScreenNavigator
            val accountSettingsFocusRequester = remember { FocusRequester() }
            val baseMenuItems = mobileMenuItems(
                showDownloads = state.hasVideoDownloads || navManager.currentRoot == RootTab.DOWNLOADS,
                order = state.mobileMainTabsOrder,
                enabled = state.mobileMainTabsEnabled,
                selectedDestination = navManager.currentRoot,
            )
            val toast = rememberGlobalToastState()
            val catalogSearchQuery by catalogSearchOpener.searchQuery.collectAsState()
            val catalogSearchFocusRequested by catalogSearchOpener.isSearchFocusPending.collectAsState()
            LaunchedEffect(effect) {
                effect.collect { eff ->
                    when (eff) {
                        is MainState.Effect.ShowToast -> toast.show(eff.message)
                    }
                }
            }

            YummyTvTheme(
                appTheme = state.appTheme,
                backgroundStyle = state.backgroundStyle,
                isTelevision = false,
            ) {
                ConfirmDialogHost(isTelevision = false)
                ChoiceDialogHost(isTelevision = false)
                val onboarding = rememberOnboardingController()
                SeasonalPrompt(
                    theme = state.seasonalPrompt,
                    isTelevision = false,
                    ready = atTabRoot && !isRequiredUpdateDestination && !isFullscreenDestination,
                    onAnswer = { disable -> onEvent(MainState.Event.SeasonalPromptAnswered(disable)) },
                )
                CompositionLocalProvider(
                    LocalPosterCardSize provides state.posterCardSize,
                    LocalOnboardingController provides onboarding,
                    LocalIsOffline provides !state.isOnline,
                    LocalSeasonalDecorationsEnabled provides state.seasonalDecorationsEnabled,
                    LocalNowMillis provides appClock::nowMillis,
                    LocalLibraryListStatuses provides state.libraryListStatuses,
                    LocalLibraryFavoriteAnimeIds provides state.libraryFavoriteAnimeIds,
                    LocalLibraryUserRatings provides state.libraryUserRatings,
                    LocalMobileBottomBarUpFocusRequester provides accountSettingsFocusRequester.takeIf {
                        atTabRoot && navManager.currentRoot == RootTab.ACCOUNT
                    },
                ) {
                    MobileMainScaffold(
                        selectedDestination = navManager.currentRoot,
                        menuItems = remember(
                            baseMenuItems,
                            state.mobileHeaderEnabled,
                            state.yaniAvatarUrl,
                            state.isYaniSignedIn,
                            state.unreadNotificationsCount,
                            state.mobileMainTabsOrder,
                            state.mobileMainTabsEnabled,
                            state.hasVideoDownloads,
                            profileTabLabel,
                        ) {
                            if (state.mobileHeaderEnabled) baseMenuItems else {
                                baseMenuItems + su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem(
                                    label = profileTabLabel,
                                    destination = RootTab.ACCOUNT,
                                    icon = Icons.Filled.Person,
                                    badgeCount = state.unreadNotificationsCount,
                                    avatarUrl = if (state.isYaniSignedIn) state.yaniAvatarUrl else "",
                                )
                            }
                        },
                        showBottomBar = atTabRoot && !isRequiredUpdateDestination &&
                            !isFullscreenDestination,
                        showRail = showRail,
                        showHeader = state.mobileHeaderEnabled && atTabRoot && !isProfileRoot &&
                            !isRequiredUpdateDestination && !isFullscreenDestination,
                        headerExpandedByDefault = navManager.currentRoot == RootTab.HOME ||
                            navManager.currentRoot == RootTab.CATALOG ||
                            navManager.currentRoot == RootTab.LIBRARY ||
                            navManager.currentRoot == RootTab.SCHEDULE,
                        // Core browsing destinations keep the global header stable. Their
                        // own controls are rendered as a compact continuation directly below it.
                        keepHeaderVisible = navManager.currentRoot == RootTab.HOME ||
                            navManager.currentRoot == RootTab.CATALOG ||
                            navManager.currentRoot == RootTab.LIBRARY ||
                            navManager.currentRoot == RootTab.SCHEDULE,
                        classicBottomBar = state.mobileClassicBottomBar,
                        searchFabEnabled = state.mobileSearchFabEnabled,
                        catalogFabEnabled = navManager.currentRoot == RootTab.CATALOG,
                        overlayHeader = false,
                        contentDrawsBehindStatusBar = isProfileRoot,
                        headerAvatarUrl = if (state.isYaniSignedIn) state.yaniAvatarUrl else "",
                        unreadNotificationsCount = state.unreadNotificationsCount,
                        searchQuery = catalogSearchQuery,
                        isCatalogSelected = navManager.currentRoot == RootTab.CATALOG,
                        isSearchFocusRequested = catalogSearchFocusRequested,
                        onSearchQueryChange = catalogSearchOpener::updateSearchQuery,
                        onSearchSubmitted = catalogSearchOpener::submitSearch,
                        onSearchFiltersSelected = catalogSearchOpener::openFilters,
                        onSearchFocusHandled = catalogSearchOpener::consumeSearchFocus,
                        onSearchSelected = { catalogSearchOpener.openSearch() },
                        onCatalogRandomSelected = { catalogSearchOpener.openRandomAnime() },
                        onNotificationsSelected = {
                            navManager.navigate(AccountNotificationsDestination)
                        },
                        onSettingsSelected = {
                            navManager.navigate(SettingsDestination)
                        },
                        onProfileSelected = {
                            onEvent(MainState.Event.RootSelected(RootTab.ACCOUNT, popToRootOnReselect = true))
                        },
                        onDestinationSelected = { root ->
                            onEvent(MainState.Event.RootSelected(root, popToRootOnReselect = true))
                        },
                        toastMessage = toast.message,
                        showSeasonalOverlay = !isFullscreenDestination,
                    ) {
                        AppNavHost(
                            navManager = navManager,
                            backStackHost = backStackHost,
                            registrars = navigationHolder.registrars,
                            modifier = Modifier.fillMaxSize(),
                            extraSceneStrategies = listOf(
                                rememberMobileListDetailSceneStrategy(currentDestination),
                            ),
                            transitionSpec = remember(state.navTransitionStyle, atTabRoot) {
                                if (atTabRoot) {
                                    { EnterTransition.None togetherWith ExitTransition.None }
                                } else {
                                    mobileNavTransitionSpec(state.navTransitionStyle)
                                }
                            },
                            popTransitionSpec = remember(state.navTransitionStyle, atTabRoot) {
                                if (atTabRoot) {
                                    { EnterTransition.None togetherWith ExitTransition.None }
                                } else {
                                    mobileNavPopTransitionSpec(state.navTransitionStyle)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
