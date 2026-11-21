package su.afk.yummy.tv.feature.main.mobile.view

import su.afk.yummy.tv.core.designsystem.seasonal.seasonalNavigationBarItemColors
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.onboarding.onboardingTarget
import su.afk.yummy.tv.feature.main.mobile.utils.mobileMainTabTargetId
import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalBarDecoration
import su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem
import su.afk.yummy.tv.feature.main.presentation.R
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun <T> MobileMainNavigationBar(
    selectedDestination: T,
    menuItems: List<MobileMenuItem<T>>,
    containerColor: Color,
    upFocusRequester: FocusRequester?,
    classicStyle: Boolean = false,
    searchFabEnabled: Boolean = true,
    catalogFabEnabled: Boolean = false,
    onSearchSelected: () -> Unit,
    onCatalogRandomSelected: () -> Unit = {},
    onDestinationSelected: (T) -> Unit,
) {
    val orderedItems = remember(menuItems) { menuItems }
    var dragPreviewIndex by remember { mutableStateOf<Float?>(null) }
    // Session-local A/B chooser: compare layouts without changing the saved preference.
    var panelVariant by remember(classicStyle) { mutableIntStateOf(if (classicStyle) 0 else 2) }
    var showVariantMenu by remember { mutableStateOf(false) }
    var showFabActions by remember { mutableStateOf(false) }
    val isClassic = panelVariant == 0 || panelVariant == 1
    val attachedFab = panelVariant == 0 || panelVariant == 4
    val expandableFab = panelVariant == 3
    LaunchedEffect(selectedDestination) { dragPreviewIndex = null }
    val selectedItemIndex = orderedItems.indexOfFirst { it.destination == selectedDestination }
        .takeIf { it >= 0 }
    val animatedPreviewIndex by animateFloatAsState(
        targetValue = dragPreviewIndex ?: (selectedItemIndex?.toFloat() ?: -1f),
        // Keep the active indicator responsive to the finger while retaining a soft settle on release.
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 900f),
        label = "navigationDragPreview",
    )
    val bottomInset = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (classicStyle) Modifier
                else Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp),
            ),
    ) {
        val navigationHeight = if (isClassic) 80.dp + bottomInset else 64.dp

        // In the modern layout the FAB is a sibling of the bar, not a second row
        // above it. Both are measured in the same Row so their vertical centers align
        // and the navigation receives exactly the width left after the FAB and gap.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (searchFabEnabled && !attachedFab) 10.dp else 0.dp),
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(navigationHeight),
                shape = if (isClassic) RoundedCornerShape(0.dp) else RoundedCornerShape(32.dp),
                color = containerColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isClassic) 80.dp else 64.dp)
                        .pointerInput(orderedItems, selectedDestination) {
                            if (orderedItems.size < 2) return@pointerInput
                            var totalDrag = 0f
                            var startIndex = 0
                            var previewIndex = 0f
                            var lastFingerX = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    totalDrag = 0f
                                    startIndex = orderedItems.indexOfFirst {
                                        it.destination == selectedDestination
                                    }.coerceAtLeast(0)
                                    lastFingerX = offset.x
                                    previewIndex = (
                                        offset.x / size.width.coerceAtLeast(1) * orderedItems.size
                                    ).coerceIn(0f, orderedItems.lastIndex.toFloat())
                                    dragPreviewIndex = previewIndex
                                },
                                onHorizontalDrag = { change, amount ->
                                    change.consume()
                                    totalDrag += amount
                                    lastFingerX = change.position.x
                                    previewIndex = (
                                        lastFingerX / size.width.coerceAtLeast(1) * orderedItems.size
                                    ).coerceIn(0f, orderedItems.lastIndex.toFloat())
                                    dragPreviewIndex = previewIndex
                                },
                                onDragEnd = {
                                    val nearest = previewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
                                    val target = if (nearest == startIndex && abs(totalDrag) > 24f) {
                                        (startIndex + if (totalDrag < 0f) 1 else -1)
                                            .coerceIn(0, orderedItems.lastIndex)
                                    } else nearest
                                    if (target != startIndex) {
                                        dragPreviewIndex = target.toFloat()
                                        onDestinationSelected(orderedItems[target].destination)
                                    } else {
                                        dragPreviewIndex = null
                                    }
                                },
                                onDragCancel = { dragPreviewIndex = null },
                            )
                        },
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0.dp),
                ) {
                    val itemColors = seasonalNavigationBarItemColors()
                    orderedItems.forEachIndexed { index, item ->
                        val selected = animatedPreviewIndex >= 0f &&
                            index == animatedPreviewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
                        NavigationBarItem(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("main_tab")
                                .onboardingTarget(mobileMainTabTargetId(item.destination))
                                .clickablePointer()
                                .focusProperties {
                                    upFocusRequester?.takeIf { selected }?.let { up = it }
                                },
                            selected = selected,
                            colors = itemColors,
                            onClick = { onDestinationSelected(item.destination) },
                            icon = {
                                if (!isClassic && selected) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        MobileMainNavigationIcon(item, selected)
                                        Text(item.label, maxLines = 1, softWrap = false)
                                    }
                                } else {
                                    MobileMainNavigationIcon(item, selected)
                                }
                            },
                            label = if (isClassic) {
                                { Text(item.label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }
                            } else null,
                            alwaysShowLabel = isClassic,
                        )
                    }
                }
            }

            if (searchFabEnabled) {
                Box {
                    FloatingActionButton(
                        onClick = {
                            if (expandableFab) showFabActions = true
                            else if (catalogFabEnabled) onCatalogRandomSelected() else onSearchSelected()
                        },
                        modifier = Modifier.size(56.dp),
                        shape = if (attachedFab) RoundedCornerShape(18.dp) else RoundedCornerShape(20.dp),
                        containerColor = if (attachedFab) containerColor else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (attachedFab) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Icon(
                            imageVector = if (catalogFabEnabled) Icons.Default.Shuffle else Icons.Default.Search,
                            contentDescription = stringResource(
                                if (catalogFabEnabled) R.string.main_header_random_anime else R.string.main_header_search,
                            ),
                        )
                    }
                    DropdownMenu(expanded = showFabActions, onDismissRequest = { showFabActions = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.main_header_search)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            onClick = { showFabActions = false; onSearchSelected() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.main_header_random_anime)) },
                            leadingIcon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                            onClick = { showFabActions = false; onCatalogRandomSelected() },
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier.align(Alignment.TopEnd)
                .offset(y = (-44).dp)
                .padding(end = if (isClassic) 8.dp else 14.dp),
        ) {
            IconButton(onClick = { showVariantMenu = true }) {
                Icon(Icons.Default.Tune, contentDescription = "Выбрать вариант панели",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(expanded = showVariantMenu, onDismissRequest = { showVariantMenu = false }) {
                listOf(
                    "Классическая · FAB встроен",
                    "Классическая · FAB отдельно",
                    "Современная · FAB справа",
                    "Современная · меню действий FAB",
                    "Современная · FAB встроен",
                ).forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        leadingIcon = { if (panelVariant == index) Icon(Icons.Default.Tune, contentDescription = null) },
                        onClick = { panelVariant = index; showFabActions = false; showVariantMenu = false },
                    )
                }
            }
        }
        SeasonalBarDecoration(Modifier.align(Alignment.TopCenter))
    }
}
