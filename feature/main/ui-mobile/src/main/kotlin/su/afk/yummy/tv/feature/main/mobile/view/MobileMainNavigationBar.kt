package su.afk.yummy.tv.feature.main.mobile.view

import su.afk.yummy.tv.core.designsystem.seasonal.seasonalNavigationBarItemColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.onboarding.onboardingTarget
import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalBarDecoration
import su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem
import su.afk.yummy.tv.feature.main.mobile.utils.mobileMainTabTargetId
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

    // The variants are intentionally independent: classic is a conventional full-width bar,
    // expressive is a floating capsule with a separate circular action button.
    var panelVariant by remember(classicStyle) {
        mutableIntStateOf(if (classicStyle) 0 else 1)
    }
    var showVariantMenu by remember { mutableStateOf(false) }
    val isClassic = panelVariant == 0

    LaunchedEffect(selectedDestination) { dragPreviewIndex = null }

    val selectedItemIndex = orderedItems.indexOfFirst { it.destination == selectedDestination }
        .takeIf { it >= 0 }
    val animatedPreviewIndex by animateFloatAsState(
        targetValue = dragPreviewIndex ?: (selectedItemIndex?.toFloat() ?: -1f),
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 900f),
        label = "navigationDragPreview",
    )

    val dragModifier = Modifier.pointerInput(orderedItems, selectedDestination) {
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
                previewIndex = (offset.x / size.width.coerceAtLeast(1) * orderedItems.size)
                    .coerceIn(0f, orderedItems.lastIndex.toFloat())
                dragPreviewIndex = previewIndex
            },
            onHorizontalDrag = { change, amount ->
                change.consume()
                totalDrag += amount
                lastFingerX = change.position.x
                previewIndex = (lastFingerX / size.width.coerceAtLeast(1) * orderedItems.size)
                    .coerceIn(0f, orderedItems.lastIndex.toFloat())
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
                } else dragPreviewIndex = null
            },
            onDragCancel = { dragPreviewIndex = null },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = if (isClassic) 0.dp else 16.dp,
                end = 16.dp,
                bottom = if (isClassic) 0.dp else 8.dp,
            ),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (searchFabEnabled) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isClassic) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                    color = containerColor,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxHeight().then(dragModifier),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0.dp),
                    ) {
                        val itemColors = seasonalNavigationBarItemColors()
                        orderedItems.forEachIndexed { index, item ->
                            val selected = animatedPreviewIndex >= 0f &&
                                index == animatedPreviewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
                            NavigationBarItem(
                                modifier = Modifier
                                    .testTag("main_tab")
                                    .onboardingTarget(mobileMainTabTargetId(item.destination))
                                    .clickablePointer()
                                    .focusProperties {
                                        upFocusRequester?.takeIf { selected }?.let { up = it }
                                    },
                                selected = selected,
                                colors = itemColors,
                                onClick = { onDestinationSelected(item.destination) },
                                icon = { MobileMainNavigationIcon(item, selected) },
                                label = {
                                    Text(
                                        item.label,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                alwaysShowLabel = true,
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .then(dragModifier),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        orderedItems.forEachIndexed { index, item ->
                            val selected = animatedPreviewIndex >= 0f &&
                                index == animatedPreviewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
                            val itemWeight by animateFloatAsState(
                                targetValue = if (selected) 1.55f else 1f,
                                animationSpec = spring(dampingRatio = 0.86f, stiffness = 700f),
                                label = "navigationItemWeight",
                            )
                            Box(
                                modifier = Modifier
                                    .weight(itemWeight)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.secondaryContainer
                                        else Color.Transparent,
                                    )
                                    .clickable { onDestinationSelected(item.destination) }
                                    .onboardingTarget(mobileMainTabTargetId(item.destination))
                                    .testTag("main_tab")
                                    .focusProperties {
                                        upFocusRequester?.takeIf { selected }?.let { up = it }
                                    }
                                    .animateContentSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    MobileMainNavigationIcon(item, selected)
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                    ) {
                                        Text(
                                            text = item.label,
                                            modifier = Modifier.padding(start = 5.dp),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (searchFabEnabled) {
                FloatingActionButton(
                    onClick = {
                        if (catalogFabEnabled) onCatalogRandomSelected()
                        else onSearchSelected()
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                        focusedElevation = 3.dp,
                        hoveredElevation = 3.dp,
                    ),
                ) {
                    Icon(
                        imageVector = if (catalogFabEnabled) Icons.Default.Shuffle else Icons.Default.Search,
                        contentDescription = stringResource(
                            if (catalogFabEnabled) R.string.main_header_random_anime
                            else R.string.main_header_search,
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = if (isClassic) (-44).dp else (-42).dp)
                .padding(end = if (isClassic) 8.dp else 72.dp),
        ) {
            IconButton(onClick = { showVariantMenu = true }) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Выбрать вариант панели",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = showVariantMenu,
                onDismissRequest = { showVariantMenu = false },
            ) {
                listOf("Классическая", "Современная · Expressive").forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        leadingIcon = {
                            if (panelVariant == index) Icon(Icons.Default.Tune, contentDescription = null)
                        },
                        onClick = {
                            panelVariant = index
                            showVariantMenu = false
                        },
                    )
                }
            }
        }

        if (isClassic) SeasonalBarDecoration(Modifier.align(Alignment.TopCenter))
    }
}
