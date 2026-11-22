package su.afk.yummy.tv.feature.schedule

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import su.afk.yummy.tv.core.common.coroutines.AppClock
import su.afk.yummy.tv.core.common.coroutines.runSuspendCatching
import su.afk.yummy.tv.core.error.api.ErrorHandler
import su.afk.yummy.tv.core.error.api.RetryStorage
import su.afk.yummy.tv.core.model.text.UiText
import su.afk.yummy.tv.core.mvi.BaseViewModel
import su.afk.yummy.tv.core.navigation.manager.INavigationManager
import su.afk.yummy.tv.core.preferences.api.YaniAccountSettingsStore
import su.afk.yummy.tv.core.preferences.settings.SettingsStore
import su.afk.yummy.tv.core.preferences.settings.contentLanguageChanges
import su.afk.yummy.tv.domain.home.usecase.ObserveLibraryNewEpisodeAnimeIdsUseCase
import su.afk.yummy.tv.domain.home.usecase.ObserveWatchedEpisodesUseCase
import su.afk.yummy.tv.domain.library.usecase.ObserveLibraryFavoriteAnimeIdsUseCase
import su.afk.yummy.tv.domain.library.usecase.ObserveLibraryItemsUseCase
import su.afk.yummy.tv.domain.schedule.usecase.GetAnimeScheduleUseCase
import su.afk.yummy.tv.feature.details.IDetailsNavigator
import su.afk.yummy.tv.feature.schedule.mapper.toTimelineUi
import su.afk.yummy.tv.feature.schedule.mapper.withSelectedDay
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject internal constructor(
    override val errorHandler: ErrorHandler,
    override val retryStorage: RetryStorage,
    private val getSchedule: GetAnimeScheduleUseCase,
    private val nav: INavigationManager,
    private val detailsNavigator: IDetailsNavigator,
    private val analytics: ScheduleAnalytics,
    private val accountSettingsStore: YaniAccountSettingsStore,
    private val appClock: AppClock,
    private val settingsStore: SettingsStore,
    private val observeLibraryNewEpisodeAnimeIds: ObserveLibraryNewEpisodeAnimeIdsUseCase,
    private val observeLibraryFavoriteAnimeIds: ObserveLibraryFavoriteAnimeIdsUseCase,
    private val observeLibraryItems: ObserveLibraryItemsUseCase,
    private val observeWatchedEpisodes: ObserveWatchedEpisodesUseCase,
) : BaseViewModel<ScheduleState.State, ScheduleState.Event, ScheduleState.Effect>() {

    override fun createInitialState() = ScheduleState.State()

    private var prioritizedAnimeIds: Set<Int> = emptySet()
    private var favoriteAnimeIds: Set<Int> = emptySet()
    private var allLibraryAnimeIds: Set<Int> = emptySet()
    private var watchedEpisodes: Map<Int, Set<Int>> = emptyMap()

    init {
        analytics.eventScreenOpened()
        load()
        settingsStore.newEpisodesSources
            .flatMapLatest { sources -> observeLibraryNewEpisodeAnimeIds(sources) }
            .onEach(::updatePrioritizedAnimeIds)
            .launchIn(viewModelScope)
        settingsStore.scheduleLibraryFilter
            .onEach { value ->
                val filter = ScheduleState.LibraryFilter.entries.firstOrNull { it.name == value }
                    ?: ScheduleState.LibraryFilter.ALL
                setState { copy(libraryFilter = filter) }
                rebuildTimeline()
            }
            .launchIn(viewModelScope)
        observeLibraryFavoriteAnimeIds()
            .onEach(::updateFavoriteAnimeIds)
            .launchIn(viewModelScope)
        observeWatchedEpisodes()
            .onEach { watched ->
                watchedEpisodes = watched
                rebuildTimeline()
            }
            .launchIn(viewModelScope)
        observeLibraryItems()
            .onEach { items ->
                allLibraryAnimeIds = items.mapTo(HashSet()) { it.animeId }
                rebuildTimeline()
            }
            .launchIn(viewModelScope)
        accountSettingsStore.contentLanguageChanges()
            .onEach { load() }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: ScheduleState.Event) {
        when (event) {
            ScheduleState.Event.BackSelected -> nav.back()

            is ScheduleState.Event.AnimeSelected -> {
                analytics.eventAnimeSelected(event.animeId)
                nav.navigate(detailsNavigator.getDetailsDest(event.animeId))
            }

            is ScheduleState.Event.HideWatchedToggled -> {
                setState { copy(hideWatched = event.enabled) }
                rebuildTimeline()
            }

            is ScheduleState.Event.LibraryFilterSelected -> {
                val enabled = event.filter == ScheduleState.LibraryFilter.LIBRARY
                analytics.eventLibraryFilterToggled(enabled)
                setState { copy(libraryFilter = event.filter) }
                viewModelScope.launch { settingsStore.setScheduleLibraryFilter(event.filter.name) }
                rebuildTimeline()
            }

            ScheduleState.Event.InLibraryFilterToggled -> {
                val next = if (currentState.libraryFilter == ScheduleState.LibraryFilter.LIBRARY) {
                    ScheduleState.LibraryFilter.ALL
                } else {
                    ScheduleState.LibraryFilter.LIBRARY
                }
                analytics.eventLibraryFilterToggled(next == ScheduleState.LibraryFilter.LIBRARY)
                setState { copy(libraryFilter = next) }
                viewModelScope.launch { settingsStore.setScheduleLibraryFilter(next.name) }
                rebuildTimeline()
            }

            is ScheduleState.Event.DateSelected -> {
                analytics.eventDateSelected(event.epochDay)
                setState {
                    copy(tvSchedule = tvSchedule.withSelectedDay(event.epochDay))
                }
            }

            ScheduleState.Event.RetrySelected -> {
                analytics.eventRetry()
                load()
            }

            ScheduleState.Event.RefreshRequested -> refresh()
        }
    }

    private fun updatePrioritizedAnimeIds(ids: Set<Int>) {
        prioritizedAnimeIds = ids
        rebuildTimeline()
    }

    private fun updateFavoriteAnimeIds(ids: Set<Int>) {
        favoriteAnimeIds = ids
        rebuildTimeline()
    }

    private fun rebuildTimeline() {
        val now = Instant.ofEpochMilli(appClock.nowMillis()).atZone(ZoneId.systemDefault())
        setState {
            copy(
                tvSchedule = days.toList().toTimelineUi(
                    now = now,
                    selectedEpochDay = tvSchedule.selectedEpochDay,
                    prioritizedAnimeIds = prioritizedAnimeIds,
                    favoriteAnimeIds = favoriteAnimeIds,
                    libraryOnly = libraryFilter == ScheduleState.LibraryFilter.LIBRARY,
                    libraryAnimeIds = allLibraryAnimeIds,
                    hideWatched = hideWatched,
                    watchedEpisodes = watchedEpisodes,
                ),
            )
        }
    }

    private var loadJob: Job? = null

    private fun refresh() {
        if (currentState.days.isEmpty()) {
            load()
            return
        }
        if (currentState.isRefreshing) return
        requestSchedule(isRefresh = true)
    }

    private fun load() {
        requestSchedule(isRefresh = false)
    }

    /**
     * Единственная точка загрузки расписания для первоначального запроса, повтора и refresh.
     *
     * Refresh сохраняет уже показанные данные при ошибке; обычная загрузка показывает ошибку.
     * Оба сценария сбрасывают противоположный индикатор, поэтому отменённый refresh не может
     * оставить экран навсегда в состоянии isRefreshing после смены языка или Retry.
     */
    private fun requestSchedule(isRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState {
                copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = if (isRefresh) error else null,
                )
            }

            runSuspendCatching { getSchedule() }.fold(
                onSuccess = { days ->
                    val now = Instant.ofEpochMilli(appClock.nowMillis())
                        .atZone(ZoneId.systemDefault())
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            days = days.toImmutableList(),
                            tvSchedule = days.toTimelineUi(
                                now = now,
                                selectedEpochDay = tvSchedule.selectedEpochDay,
                                prioritizedAnimeIds = prioritizedAnimeIds,
                                favoriteAnimeIds = favoriteAnimeIds,
                                libraryOnly = libraryFilter == ScheduleState.LibraryFilter.LIBRARY,
                                libraryAnimeIds = allLibraryAnimeIds,
                                hideWatched = hideWatched,
                                watchedEpisodes = watchedEpisodes,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    analytics.eventLoadError(throwable)
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = if (isRefresh && days.isNotEmpty()) {
                                error
                            } else {
                                UiText.Failure(throwable)
                            },
                        )
                    }
                },
            )
        }
    }
}
