package su.afk.yummy.tv.feature.settings.mobile.view.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.model.settings.LibraryContinueWatchingCardSize
import su.afk.yummy.tv.core.model.settings.PosterCardSize
import su.afk.yummy.tv.core.model.settings.TitleCardLayout
import su.afk.yummy.tv.feature.settings.SettingsState
import su.afk.yummy.tv.feature.settings.common.utils.hint
import su.afk.yummy.tv.feature.settings.common.utils.icon
import su.afk.yummy.tv.feature.settings.common.utils.label
import su.afk.yummy.tv.feature.settings.common.utils.newEpisodesSourcesValue
import su.afk.yummy.tv.feature.settings.mobile.model.SettingsMobilePicker
import su.afk.yummy.tv.feature.settings.mobile.utils.hint
import su.afk.yummy.tv.feature.settings.mobile.utils.label
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileIconChoice
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileIconChoiceRow
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileNavigationRow
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileOptionRow
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileSection
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileToggleRow
import su.afk.yummy.tv.feature.settings.common.utils.enabledHint
import su.afk.yummy.tv.feature.settings.presentation.R

@Composable
internal fun SettingsMobileAppearanceContent(
    state: SettingsState.State,
    onEvent: (SettingsState.Event) -> Unit,
    onPickerRequested: (SettingsMobilePicker) -> Unit,
) {
    var showMainTabsEditor by remember { mutableStateOf(false) }
    var draftMainTabsOrder by remember(state.mobileMainTabsOrder) { mutableStateOf(state.mobileMainTabsOrder) }
    var draftMainTabsEnabled by remember(state.mobileMainTabsEnabled) { mutableStateOf(state.mobileMainTabsEnabled) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SettingsMobileSection(title = stringResource(R.string.settings_section_general_appearance)) {
            SettingsMobileOptionRow(
                label = stringResource(R.string.settings_tab_theme),
                value = state.appTheme.label(),
                hint = state.appTheme.hint(),
                onClick = { onPickerRequested(SettingsMobilePicker.THEME) },
            )
            SettingsMobileOptionRow(
                label = stringResource(R.string.settings_tab_background),
                value = state.backgroundStyle.label(),
                hint = state.backgroundStyle.hint(),
                onClick = { onPickerRequested(SettingsMobilePicker.BACKGROUND) },
            )
            SettingsMobileOptionRow(
                label = stringResource(R.string.settings_nav_transition_title),
                value = state.navTransitionStyle.label(),
                hint = state.navTransitionStyle.hint(),
                onClick = { onPickerRequested(SettingsMobilePicker.NAV_TRANSITION) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_seasonal_decorations),
                hint = if (state.seasonalDecorationsEnabled) {
                    state.seasonalTheme.enabledHint()
                } else {
                    stringResource(R.string.settings_disabled)
                },
                enabled = state.seasonalDecorationsEnabled,
                onClick = { onEvent(SettingsState.Event.SeasonalDecorationsToggled) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_account_shortcuts_expanded),
                hint = stringResource(R.string.settings_account_shortcuts_expanded_hint),
                enabled = state.accountShortcutsExpanded,
                onClick = { onEvent(SettingsState.Event.AccountShortcutsToggled) },
            )
        }
        SettingsMobileSection(title = stringResource(R.string.settings_mobile_navigation_section)) {
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_mobile_header_enabled),
                hint = stringResource(R.string.settings_mobile_header_enabled_hint),
                enabled = state.mobileHeaderEnabled,
                onClick = { onEvent(SettingsState.Event.MobileHeaderToggled) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_mobile_search_fab_enabled),
                hint = stringResource(R.string.settings_mobile_search_fab_enabled_hint),
                enabled = state.mobileSearchFabEnabled,
                onClick = { onEvent(SettingsState.Event.MobileSearchFabToggled) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_mobile_classic_bottom_bar),
                hint = stringResource(R.string.settings_mobile_classic_bottom_bar_hint),
                enabled = state.mobileClassicBottomBar,
                onClick = { onEvent(SettingsState.Event.MobileClassicBottomBarToggled) },
            )
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_mobile_main_tabs_customize),
                hint = stringResource(R.string.settings_mobile_main_tabs_customize_hint),
                onClick = {
                    draftMainTabsOrder = state.mobileMainTabsOrder
                    draftMainTabsEnabled = state.mobileMainTabsEnabled
                    showMainTabsEditor = true
                },
            )
        }
        SettingsMobileSection(title = stringResource(R.string.settings_section_posters)) {
            SettingsMobileIconChoiceRow(
                label = stringResource(R.string.settings_mobile_poster_size),
                hint = state.posterCardSize.hint(),
                choices = PosterCardSize.entries.map { SettingsMobileIconChoice(it, it.icon(), it.label()) },
                selected = state.posterCardSize,
                onSelected = { onEvent(SettingsState.Event.PosterCardSizeSelected(it)) },
            )
            SettingsMobileIconChoiceRow(
                label = stringResource(R.string.settings_title_card_layout_title),
                hint = state.titleCardLayout.hint(),
                choices = TitleCardLayout.entries.map { SettingsMobileIconChoice(it, it.icon(), it.label()) },
                selected = state.titleCardLayout,
                onSelected = { onEvent(SettingsState.Event.TitleCardLayoutSelected(it)) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_show_catalog_title_year),
                hint = if (state.showCatalogTitleYear) {
                    stringResource(R.string.settings_show_catalog_title_year_enabled)
                } else {
                    stringResource(R.string.settings_disabled)
                },
                enabled = state.showCatalogTitleYear,
                onClick = { onEvent(SettingsState.Event.ShowCatalogTitleYearToggled) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_show_episode_flags),
                hint = if (state.showEpisodeFlags) {
                    stringResource(R.string.settings_show_episode_flags_enabled)
                } else {
                    stringResource(R.string.settings_disabled)
                },
                enabled = state.showEpisodeFlags,
                onClick = { onEvent(SettingsState.Event.ShowEpisodeFlagsToggled) },
            )
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_show_library_title_year),
                hint = if (state.showLibraryTitleYear) {
                    stringResource(R.string.settings_show_library_title_year_enabled)
                } else {
                    stringResource(R.string.settings_disabled)
                },
                enabled = state.showLibraryTitleYear,
                onClick = { onEvent(SettingsState.Event.ShowLibraryTitleYearToggled) },
            )
        }
        SettingsMobileSection(title = stringResource(R.string.settings_mobile_section_library)) {
            SettingsMobileIconChoiceRow(
                label = stringResource(R.string.settings_library_continue_watching_card_size_title),
                hint = state.libraryContinueWatchingCardSize.hint(),
                choices = LibraryContinueWatchingCardSize.entries.map {
                    SettingsMobileIconChoice(it, it.icon(), it.label())
                },
                selected = state.libraryContinueWatchingCardSize,
                onSelected = { onEvent(SettingsState.Event.LibraryContinueWatchingCardSizeSelected(it)) },
            )
        }
        SettingsMobileSection(title = stringResource(R.string.settings_section_sections_order)) {
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_library_tabs_order),
                hint = stringResource(R.string.settings_library_tabs_order_hint),
                onClick = { onEvent(SettingsState.Event.LibraryTabOrderSelected) },
            )
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_home_blocks_title),
                hint = stringResource(R.string.settings_home_blocks_hint),
                onClick = { onEvent(SettingsState.Event.HomeBlocksSelected) },
            )
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_details_sections_title),
                hint = stringResource(R.string.settings_details_sections_hint),
                onClick = { onEvent(SettingsState.Event.DetailsSectionsSelected) },
            )
        }
        SettingsMobileSection(title = stringResource(R.string.settings_section_new_episodes)) {
            SettingsMobileToggleRow(
                label = stringResource(R.string.settings_new_episodes_section),
                hint = if (state.newEpisodesSectionEnabled) {
                    stringResource(R.string.settings_new_episodes_section_enabled)
                } else {
                    stringResource(R.string.settings_disabled)
                },
                enabled = state.newEpisodesSectionEnabled,
                onClick = { onEvent(SettingsState.Event.NewEpisodesSectionToggled) },
            )
            // Список источников без включённого блока ни на что не влияет.
            if (state.newEpisodesSectionEnabled) {
                SettingsMobileOptionRow(
                    label = stringResource(R.string.settings_new_episodes_sources_title),
                    value = state.newEpisodesSources.newEpisodesSourcesValue(),
                    onClick = { onPickerRequested(SettingsMobilePicker.NEW_EPISODES_SOURCES) },
                )
            }
        }
    }
    if (showMainTabsEditor) {
        MobileMainTabsCustomizationDialog(
            order = draftMainTabsOrder,
            enabled = draftMainTabsEnabled,
            onOrderChanged = { draftMainTabsOrder = it },
            onEnabledChanged = { draftMainTabsEnabled = it },
            onDismiss = { showMainTabsEditor = false },
            onSave = {
                onEvent(SettingsState.Event.MobileMainTabsOrderChanged(draftMainTabsOrder))
                onEvent(SettingsState.Event.MobileMainTabsEnabledChanged(draftMainTabsEnabled))
                showMainTabsEditor = false
            },
        )
    }
}

@Composable
private fun MobileMainTabsCustomizationDialog(
    order: List<String>,
    enabled: Set<String>,
    onOrderChanged: (List<String>) -> Unit,
    onEnabledChanged: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_mobile_main_tabs_customize)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_mobile_main_tabs_dialog_hint),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                order.forEachIndexed { index, key ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = key in enabled,
                            onCheckedChange = { checked ->
                                onEnabledChanged(if (checked) enabled + key else enabled - key)
                            },
                        )
                        Text(
                            text = when (key) {
                                "SCHEDULE" -> stringResource(R.string.settings_mobile_main_tab_schedule)
                                "HOME" -> stringResource(R.string.settings_mobile_main_tab_home)
                                "LIBRARY" -> stringResource(R.string.settings_mobile_main_tab_library)
                                "DOWNLOADS" -> stringResource(R.string.settings_mobile_main_tab_downloads)
                                "POSTS" -> stringResource(R.string.settings_mobile_main_tab_news)
                                "COLLECTIONS" -> stringResource(R.string.settings_mobile_main_tab_collections)
                                else -> key
                            },
                            modifier = Modifier.weight(1f),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        )
                        IconButton(
                            enabled = index > 0,
                            onClick = {
                                val next = order.toMutableList()
                                val item = next.removeAt(index)
                                next.add(index - 1, item)
                                onOrderChanged(next)
                            },
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                        }
                        IconButton(
                            enabled = index < order.lastIndex,
                            onClick = {
                                val next = order.toMutableList()
                                val item = next.removeAt(index)
                                next.add(index + 1, item)
                                onOrderChanged(next)
                            },
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.settings_mobile_main_tabs_save))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    onOrderChanged(listOf("SCHEDULE", "HOME", "LIBRARY", "DOWNLOADS", "POSTS", "COLLECTIONS"))
                    onEnabledChanged(setOf("SCHEDULE", "HOME", "LIBRARY", "DOWNLOADS"))
                }) {
                    Text(stringResource(R.string.settings_mobile_main_tabs_reset))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.settings_mobile_main_tabs_cancel))
                }
            }
        },
    )
}
