package su.afk.yummy.tv.feature.details.subscriptions

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import su.afk.yummy.tv.core.error.api.ErrorHandler
import su.afk.yummy.tv.core.common.coroutines.runSuspendCatching
import su.afk.yummy.tv.core.error.api.RetryStorage
import su.afk.yummy.tv.core.model.anime.AnimeVideo
import su.afk.yummy.tv.core.model.text.UiText
import su.afk.yummy.tv.core.mvi.BaseViewModel
import su.afk.yummy.tv.core.navigation.manager.INavigationManager
import su.afk.yummy.tv.core.preferences.settings.AppLifecycleSettingsStore
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTracking
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingMode
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingStore
import su.afk.yummy.tv.domain.account.model.SubscriptionKeys
import su.afk.yummy.tv.domain.anime.usecase.GetAnimeDetailsUseCase
import su.afk.yummy.tv.domain.anime.usecase.GetAnimeVideosUseCase
import su.afk.yummy.tv.feature.details.DetailsAnalytics
import su.afk.yummy.tv.feature.details.details.handler.DetailsSubscriptionHandler
import su.afk.yummy.tv.feature.details.details.handler.ScreenSubscriptionBaseResult

@HiltViewModel(assistedFactory = SubscriptionsViewModel.Factory::class)
class SubscriptionsViewModel @AssistedInject internal constructor(
    @Assisted private val animeId: Int,
    override val errorHandler: ErrorHandler,
    override val retryStorage: RetryStorage,
    private val nav: INavigationManager,
    private val subscriptionHandler: DetailsSubscriptionHandler,
    private val analytics: DetailsAnalytics,
    private val appLifecycleSettingsStore: AppLifecycleSettingsStore,
    private val titleReleaseTrackingStore: TitleReleaseTrackingStore,
    private val getAnimeDetails: GetAnimeDetailsUseCase,
    private val getAnimeVideos: GetAnimeVideosUseCase,
) : BaseViewModel<SubscriptionsState.State, SubscriptionsState.Event, SubscriptionsState.Effect>() {

    @AssistedFactory
    interface Factory {
        fun create(animeId: Int): SubscriptionsViewModel
    }

    override fun createInitialState() = SubscriptionsState.State()

    private var availableVideos: List<AnimeVideo> = emptyList()

    init {
        analytics.eventSubscriptionsScreenOpened(animeId)
        appLifecycleSettingsStore.notificationPermissionRequested
            .onEach { requested -> setState { copy(notificationPermissionRequested = requested) } }
            .launchIn(viewModelScope)
        titleReleaseTrackingStore.subscriptions
            .onEach { subscriptions ->
                val tracking = subscriptions.firstOrNull { it.animeId == animeId }
                setState {
                    copy(
                        isLocalTrackingEnabled = tracking != null,
                        localTrackingMode = tracking?.mode ?: TitleReleaseTrackingMode.ALL,
                        excludedDubbingKeys = tracking?.excludedDubbingKeys?.toImmutableSet()
                            ?: persistentSetOf(),
                        title = title.ifBlank { tracking?.title.orEmpty() },
                    )
                }
            }
            .launchIn(viewModelScope)
        viewModelScope.launch { load() }
    }

    override fun onEvent(event: SubscriptionsState.Event) {
        when (event) {
            SubscriptionsState.Event.BackSelected -> nav.back()
            SubscriptionsState.Event.RetrySelected -> {
                analytics.eventSubscriptionsRetry(animeId)
                viewModelScope.launch { load() }
            }
            is SubscriptionsState.Event.SubscriptionToggled -> toggleSubscription(event.key)
            is SubscriptionsState.Event.LocalTrackingToggled -> {
                setState { copy(isLocalTrackingEnabled = event.enabled) }
                saveLocalTracking()
            }
            is SubscriptionsState.Event.LocalTrackingModeSelected -> {
                setState { copy(localTrackingMode = event.mode) }
                saveLocalTracking()
            }
            is SubscriptionsState.Event.LocalDubbingExclusionToggled -> {
                setState {
                    copy(
                        excludedDubbingKeys = if (event.excluded) {
                            excludedDubbingKeys.add(event.key)
                        } else {
                            excludedDubbingKeys.remove(event.key)
                        },
                    )
                }
                saveLocalTracking()
            }
            SubscriptionsState.Event.NotificationPermissionRequested -> viewModelScope.launch {
                appLifecycleSettingsStore.markNotificationPermissionRequested()
            }
        }
    }

    private suspend fun load(showLoading: Boolean = true) {
        if (showLoading) setState { copy(isLoading = true, error = null) }

        // Local tracking is device-scoped and works for guests too. Fetch the public video list
        // independently from the account-only server subscription list.
        val details = runSuspendCatching { getAnimeDetails(animeId) }.getOrNull()
        val videosResult = runSuspendCatching { getAnimeVideos(animeId) }
        availableVideos = videosResult.getOrDefault(emptyList())
        val currentTracking = titleReleaseTrackingStore.subscriptions.first()
            .firstOrNull { it.animeId == animeId }
        setState {
            copy(
                title = details?.title?.takeIf { it.isNotBlank() }
                    ?: currentTracking?.title?.takeIf { it.isNotBlank() }
                    ?: "Аниме #$animeId",
                isLocalTrackingLoading = false,\n                localTrackingAvailable = videosResult.isSuccess,
                availableDubbings = availableVideos.toTrackingDubbings().toImmutableList(),
            )
        }

        when (val result = subscriptionHandler.loadScreenSubscriptionBase(animeId)) {
            ScreenSubscriptionBaseResult.SignedOut -> {
                setState {
                    copy(
                        isLoading = false,
                        isSignedIn = false,
                        subscriptions = persistentListOf(),
                    )
                }
            }
            is ScreenSubscriptionBaseResult.Content -> {
                setState {
                    copy(
                        isLoading = false,
                        isSignedIn = true,
                        error = null,
                        subscriptions = result.base.subscriptions.toImmutableList(),
                    )
                }
            }
            is ScreenSubscriptionBaseResult.Failure -> {
                analytics.eventSubscriptionsLoadError(result.error)
                setState {
                    copy(
                        isLoading = false,
                        isSignedIn = true,
                        error = UiText.Failure(result.error),
                        subscriptions = persistentListOf(),
                    )
                }
            }
        }
    }

    private fun saveLocalTracking() {
        viewModelScope.launch {
            val state = currentState
            if (!state.isLocalTrackingEnabled) {
                titleReleaseTrackingStore.remove(animeId)
                return@launch
            }
            val previous = titleReleaseTrackingStore.subscriptions.first()
                .firstOrNull { it.animeId == animeId }
            val excluded = state.excludedDubbingKeys
            val baseline = availableVideos.asSequence()
                .filter { video ->
                    SubscriptionKeys.dubbingKey(video.dubbing) !in excluded &&
                        when (state.localTrackingMode) {
                            TitleReleaseTrackingMode.ALL -> true
                            TitleReleaseTrackingMode.DUBBING_ONLY -> !video.dubbing.isSubtitleLabel()
                            TitleReleaseTrackingMode.SUBTITLES_ONLY -> video.dubbing.isSubtitleLabel()
                        }
                }
                .mapNotNull { it.episode.toRegularEpisodeNumber() }
                .toSet()
            titleReleaseTrackingStore.upsert(
                TitleReleaseTracking(
                    animeId = animeId,
                    title = state.title,
                    mode = state.localTrackingMode,
                    excludedDubbingKeys = excluded,
                    knownEpisodeNumbers = previous?.knownEpisodeNumbers.orEmpty() + baseline,
                    initialized = true,
                ),
            )
        }
    }

    private fun List<AnimeVideo>.toTrackingDubbings(): List<TitleReleaseDubbingOption> =
        filter { it.dubbing.isNotBlank() }
            .groupBy { SubscriptionKeys.dubbingKey(it.dubbing) }
            .map { (key, videos) ->
                TitleReleaseDubbingOption(
                    key = key,
                    name = videos.first().dubbing,
                    isSubtitles = videos.first().dubbing.isSubtitleLabel(),
                    episodesCount = videos.mapNotNull { it.episode.toRegularEpisodeNumber() }.distinct().size,
                )
            }
            .sortedWith(compareByDescending<TitleReleaseDubbingOption> { it.episodesCount }.thenBy { it.name })

    private fun String.isSubtitleLabel(): Boolean {
        val normalized = trim().lowercase().replace('ё', 'е')
        return normalized.contains("субтитр") ||
            normalized.contains("subtitle") ||
            normalized == "sub" ||
            normalized.startsWith("sub ")
    }

    private fun String.toRegularEpisodeNumber(): Int? {
        val match = EPISODE_NUMBER_PATTERN.matchEntire(trim()) ?: return null
        val whole = match.groupValues[1]
        val fraction = match.groupValues[2]
        val value = (if (fraction.isEmpty()) whole else "$whole.$fraction")
            .toBigDecimalOrNull() ?: return null
        if (value.stripTrailingZeros().scale() > 0) return null
        return runCatching { value.intValueExact() }.getOrNull()?.takeIf { it > 0 }
    }

    private fun toggleSubscription(key: String) {
        val option = currentState.subscriptions.firstOrNull { it.key == key } ?: return
        val wasSubscribed = option.isSubscribed
        analytics.eventSubscriptionsSubscriptionToggled(
            animeId = animeId,
            videoId = option.subscriptionVideoId,
            targetState = !wasSubscribed,
        )
        setSubscriptionState(key, !wasSubscribed)
        viewModelScope.launch {
            val changed = subscriptionHandler.commitSubscriptionChange(
                animeId = animeId,
                option = option,
                subscribed = !wasSubscribed,
            )
            if (!changed) {
                setSubscriptionState(key, wasSubscribed)
                return@launch
            }
            subscriptionHandler.reloadSubscriptions(animeId).onSuccess { subscriptions ->
                setState { copy(subscriptions = subscriptions.toImmutableList()) }
            }
        }
    }

    private fun setSubscriptionState(key: String, subscribed: Boolean) {
        setState {
            copy(
                subscriptions = subscriptions.map {
                    if (it.key == key) it.copy(isSubscribed = subscribed) else it
                }.toImmutableList(),
            )
        }
    }

    private companion object {
        val EPISODE_NUMBER_PATTERN = Regex(
            """^(\d+)(?:[.,](\d+))?\s*(?:серия|серии|эпизод|эп\.?|episode|ep\.?)?$""",
            RegexOption.IGNORE_CASE,
        )
    }
}
