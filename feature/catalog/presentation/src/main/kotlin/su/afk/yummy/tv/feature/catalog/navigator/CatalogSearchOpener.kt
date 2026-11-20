package su.afk.yummy.tv.feature.catalog.navigator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import su.afk.yummy.tv.core.navigation.manager.INavigationManager
import su.afk.yummy.tv.core.navigation.root.RootTab
import su.afk.yummy.tv.feature.catalog.ICatalogSearchOpener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CatalogSearchOpener @Inject constructor(
    private val nav: INavigationManager,
) : ICatalogSearchOpener {
    private val pending = MutableStateFlow(false)
    private val randomAnimePending = MutableStateFlow(false)

    override val isRandomAnimePending: StateFlow<Boolean> = randomAnimePending.asStateFlow()

    override val isSearchFocusPending: StateFlow<Boolean> = pending.asStateFlow()

    override fun openSearch() {
        pending.value = true
        // Новый поиск начинается с чистого листа: цепочки тайтлов, в которые можно уйти из поиска
        // и с главной, сносим на обеих вкладках, откуда бы ни вызвали. Остальные вкладки не трогаем.
        nav.resetRoot(RootTab.CATALOG)
        nav.switchRoot(RootTab.CATALOG, reselectPopToRoot = true)
    }

    override fun consumeSearchFocus() {
        pending.value = false
    }

    override fun openRandomAnime() {
        randomAnimePending.value = true
        nav.resetRoot(RootTab.CATALOG)
        nav.switchRoot(RootTab.CATALOG, reselectPopToRoot = true)
    }

    override fun consumeRandomAnimeRequest() {
        randomAnimePending.value = false
    }
}
