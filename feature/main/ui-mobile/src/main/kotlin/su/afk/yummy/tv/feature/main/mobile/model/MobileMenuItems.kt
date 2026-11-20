package su.afk.yummy.tv.feature.main.mobile.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.navigation.root.RootTab
import su.afk.yummy.tv.feature.main.presentation.R

/** Четыре основных мобильных раздела; профиль, поиск и настройки доступны из глобальной шапки. */
@Composable
internal fun mobileMenuItems(): List<MobileMenuItem<RootTab>> = listOf(
    MobileMenuItem(
        stringResource(R.string.main_tab_news),
        RootTab.POSTS,
        Icons.Default.Newspaper,
    ),
    MobileMenuItem(
        stringResource(R.string.main_tab_catalog),
        RootTab.CATALOG,
        Icons.Default.GridView,
    ),
    MobileMenuItem(
        stringResource(R.string.main_tab_home),
        RootTab.HOME,
        Icons.Default.Home,
        hasSeasonalDecor = true,
    ),
    MobileMenuItem(
        stringResource(R.string.main_tab_library),
        RootTab.LIBRARY,
        Icons.AutoMirrored.Filled.List,
    ),
)
