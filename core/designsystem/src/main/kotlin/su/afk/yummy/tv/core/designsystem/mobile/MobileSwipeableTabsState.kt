package su.afk.yummy.tv.core.designsystem.mobile

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Состояние вкладок, основанных на [PagerState].
 *
 * Для определения выбранной страницы используется [settledPage]: [PagerState.currentPage]
 * меняется ещё во время перетаскивания и не означает, что пользователь завершил переход.
 */
@Stable
class MobileSwipeableTabsState internal constructor(
    val pagerState: PagerState,
    private val scope: CoroutineScope,
) {
    /** Страница, до которой pager завершил прокрутку. */
    val settledPage: Int get() = pagerState.settledPage

    /** Совместимый псевдоним для выбранной, уже зафиксированной страницы. */
    val currentPage: Int get() = settledPage

    /** Анимированно переключает pager на [index], игнорируя недопустимые и повторные запросы. */
    fun selectPage(index: Int) {
        if (index !in 0 until pagerState.pageCount) return
        if (pagerState.settledPage == index || pagerState.targetPage == index) return

        scope.launch {
            pagerState.animateScrollToPage(index)
        }
    }
}

/**
 * Запоминает состояние pager-вкладок и синхронизирует его с внешним выбранным индексом.
 *
 * Внешнее состояние обновляется только после завершения прокрутки. Промежуточные значения
 * [PagerState.currentPage] во время жеста не запускают встречные анимации.
 *
 * Поддерживает динамическое изменение [pageCount], в том числе пустой набор страниц.
 * Внешний [selectedPage] автоматически ограничивается допустимым диапазоном.
 */
@Composable
fun rememberMobileSwipeableTabsState(
    selectedPage: Int,
    pageCount: Int,
    onPageSelected: (Int) -> Unit,
): MobileSwipeableTabsState {
    val safePageCount = pageCount.coerceAtLeast(0)
    val safeSelectedPage = selectedPage.coerceIn(0, (safePageCount - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = safeSelectedPage,
        pageCount = { safePageCount },
    )
    val scope = rememberCoroutineScope()
    val state = remember(pagerState, scope) { MobileSwipeableTabsState(pagerState, scope) }

    val currentOnPageSelected = rememberUpdatedState(onPageSelected)
    val currentSelectedPage = rememberUpdatedState(safeSelectedPage)

    // Внешний выбор -> pager. При новом выборе устаревшая анимация отменяется.
    LaunchedEffect(safeSelectedPage, safePageCount, pagerState) {
        if (safePageCount > 0 && pagerState.settledPage != safeSelectedPage) {
            pagerState.animateScrollToPage(safeSelectedPage)
        }
    }

    // Pager -> внешнее состояние. Публикуем только завершённые переходы.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                if (page in 0 until pagerState.pageCount &&
                    page != currentSelectedPage.value
                ) {
                    currentOnPageSelected.value(page)
                }
            }
    }

    return state
}
