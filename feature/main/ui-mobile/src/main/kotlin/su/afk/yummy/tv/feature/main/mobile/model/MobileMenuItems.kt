package su.afk.yummy.tv.feature.main.mobile.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Forum
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.navigation.root.RootTab
import su.afk.yummy.tv.feature.main.presentation.R

/** Основные мобильные разделы. Новости остаются в «Ещё» на главной, Расписание вынесено в меню. */
@Composable
internal fun mobileMenuItems(
    showDownloads: Boolean,
    order: List<String>,
    enabled: Set<String>,
    selectedDestination: RootTab,
): List<MobileMenuItem<RootTab>> {
    val available = listOf(
        "SCHEDULE" to MobileMenuItem(stringResource(R.string.main_tab_schedule), RootTab.SCHEDULE, Icons.Default.CalendarToday),
        "HOME" to MobileMenuItem(stringResource(R.string.main_tab_home), RootTab.HOME, Icons.Default.Home, hasSeasonalDecor = true),
        "COMMUNITY" to MobileMenuItem(stringResource(R.string.main_tab_community), RootTab.COMMUNITY, Icons.Default.Forum),
        "LIBRARY" to MobileMenuItem(stringResource(R.string.main_tab_library), RootTab.LIBRARY, Icons.AutoMirrored.Filled.List),
        "POSTS" to MobileMenuItem(stringResource(R.string.main_tab_news), RootTab.POSTS, Icons.Default.Article),
        "COLLECTIONS" to MobileMenuItem(stringResource(R.string.main_tab_collections), RootTab.COLLECTIONS, Icons.Default.CollectionsBookmark),
    ).toMap() + if (showDownloads) {
        mapOf("DOWNLOADS" to MobileMenuItem(stringResource(R.string.main_tab_downloads), RootTab.DOWNLOADS, Icons.Default.Download))
    } else emptyMap()
    val ordered = order.asSequence()
        .filter { it in enabled }
        .mapNotNull(available::get)
        .toMutableList()
    // A destination that was just hidden must remain reachable until the user leaves it.
    if (ordered.none { it.destination == selectedDestination }) {
        available.values.firstOrNull { it.destination == selectedDestination }?.let(ordered::add)
    }
    if (ordered.isEmpty()) available["HOME"]?.let(ordered::add)
    return ordered
}
