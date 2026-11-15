package su.afk.yummy.tv.core.designsystem.mobile

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect

/**
 * Состояние вкладок, основанных на [PagerState].
 *
 * [settledPage] — последняя полностью показанная страница. [PagerState.currentPage] может
 * меняться во время жеста и поэтому не используется как подтверждённый выбор.
 */
@Stable
class MobileSwipeableTabsState internal constructor(
    val pagerState: PagerState,
    private val onPageSelected: (Int) -> Unit,
) {
    val settledPage: Int get() = pagerState.settledPage

    /** Совместимый псевдоним для ранее опубликованного API. */
    val currentPage: Int get() = settledPage

    /**
     * Запрашивает выбор страницы через внешний state holder.
     *
     * Сам pager не прокручивается здесь: единственный владелец программной прокрутки —
     * эффект, наблюдающий за selectedPage. Это исключает конкурирующие animateScrollToPage.
     */
    fun selectPage(index: Int) {
        if (index !in 0 until pagerState.pageCount) return
        if (index == pagerState.settledPage) return
        onPageSelected(index)
    }
}

/**
 * Создаёт pager state и обеспечивает однонаправленную синхронизацию:
 *
 * - внешний selectedPage управляет программной прокруткой;
 * - завершённый свайп сообщает новый выбор наружу;
 * - промежуточные currentPage/targetPage не меняют внешний state.
 *
 * [pageCount] может изменяться во время жизни composable. Индекс выбора ограничивается
 * актуальным диапазоном. При пустом наборе страниц pager остаётся без контента и не запускает
 * прокрутку.
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

    val currentOnPageSelected = rememberUpdatedState(onPageSelected)
    val currentSelectedPage = rememberUpdatedState(safeSelectedPage)
    val state = remember(pagerState) {
        MobileSwipeableTabsState(pagerState) { page ->
            currentOnPageSelected.value(page)
        }
    }

    // Внешний state — единственный источник программных переходов.
    // Изменение selectedPage отменяет устаревшую анимацию предыдущего выбора.
    LaunchedEffect(pagerState, safeSelectedPage, safePageCount) {
        if (
            safePageCount > 0 &&
            safeSelectedPage in 0 until pagerState.pageCount &&
            pagerState.settledPage != safeSelectedPage
        ) {
            pagerState.animateScrollToPage(safeSelectedPage)
        }
    }

    // Обратная синхронизация выполняется только после фактического завершения свайпа.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                if (
                    page in 0 until pagerState.pageCount &&
                    page != currentSelectedPage.value
                ) {
                    currentOnPageSelected.value(page)
                }
            }
    }

    return state
}
