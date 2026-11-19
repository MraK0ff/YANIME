package su.afk.yummy.tv.domain.home.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import su.afk.yummy.tv.core.testing.BaseUnitTest
import su.afk.yummy.tv.domain.home.model.HomeFeedItemAction
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleItem
import su.afk.yummy.tv.domain.schedule.repository.AnimeScheduleRepository
import su.afk.yummy.tv.domain.schedule.usecase.GetAnimeScheduleUseCase

class GetRecentlyAiredScheduleUseCaseTest : BaseUnitTest() {

    private val repository: AnimeScheduleRepository = mockk()
    private val schedule = mutableListOf<AnimeScheduleItem>()

    @Before
    fun setUp() {
        coEvery { repository.getSchedule() } answers {
            listOf(AnimeScheduleDay(title = "Monday", items = schedule.toList()))
        }
    }

    @Test
    fun `episodes inside the video publication window are returned freshest first`() = runTest {
        schedule += scheduleItem(animeId = 1, releases = listOf(release(1, NOW - 10 * DAY)))
        schedule += scheduleItem(animeId = 2, releases = listOf(release(2, NOW - DAY)))

        assertEquals(listOf(2, 1), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `episodes older than the video publication window are dropped`() = runTest {
        schedule += scheduleItem(animeId = 1, releases = listOf(release(1, NOW - 15 * DAY)))

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `the video publication window boundary is inclusive`() = runTest {
        schedule += scheduleItem(animeId = 1, releases = listOf(release(1, NOW - 14 * DAY)))

        assertEquals(listOf(1), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `Japanese aired count without an actual video does not create a release`() = runTest {
        schedule += scheduleItem(animeId = 1, airedEpisodes = 12, releases = emptyList())

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `a title appears once with its freshest actually published episode`() = runTest {
        schedule += scheduleItem(
            animeId = 1,
            airedEpisodes = 7,
            releases = listOf(
                release(1, NOW - 2 * DAY, episode = 7),
                release(1, NOW - DAY, episode = 8),
            ),
        )

        val result = createUseCase()(NOW)

        assertEquals(listOf(1), result.map { it.id })
        assertEquals(8, result.single().episodeNumber)
        assertEquals(NOW - DAY, result.single().airedAtSeconds)
    }

    @Test
    fun `late dub of an episode older than the aired count is ignored`() = runTest {
        schedule += scheduleItem(
            animeId = 1,
            airedEpisodes = 8,
            releases = listOf(release(1, NOW - DAY, episode = 6)),
        )

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `episode equal to the aired count is not confirmed as new`() = runTest {
        schedule += scheduleItem(
            animeId = 1,
            airedEpisodes = 8,
            releases = listOf(release(1, NOW - DAY, episode = 8)),
        )

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `cached anime page count overrides a stale lower schedule count`() = runTest {
        schedule += scheduleItem(
            animeId = 1,
            airedEpisodes = 6,
            availableEpisodeCount = 8,
            releases = listOf(release(1, NOW - DAY, episode = 6)),
        )

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `late dub of the latest already cached episode is ignored`() = runTest {
        schedule += scheduleItem(
            animeId = 1,
            airedEpisodes = 8,
            availableEpisodeCount = 8,
            releases = listOf(release(1, NOW - DAY, episode = 8)),
        )

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `unknown aired count fails closed`() = runTest {
        schedule += scheduleItem(animeId = 1, airedEpisodes = null, releases = listOf(release(1, NOW - DAY)))

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `future-dated or invalid release observations are not treated as released`() = runTest {
        schedule += scheduleItem(
            animeId = 1,
            releases = listOf(
                release(1, NOW + DAY),
                release(1, 0L, episode = 2),
            ),
        )

        assertEquals(emptyList<Int>(), createUseCase()(NOW).map { it.id })
    }

    @Test
    fun `episode number and first video publication time are carried over`() = runTest {
        schedule += scheduleItem(animeId = 7, releases = listOf(release(7, NOW - DAY, episode = 3)))

        val item = createUseCase()(NOW).single()

        assertEquals(3, item.episodeNumber)
        assertEquals(NOW - DAY, item.airedAtSeconds)
        assertEquals(HomeFeedItemAction.OpenSeries(7), item.action)
    }

    private fun createUseCase() = GetRecentlyAiredScheduleUseCase(GetAnimeScheduleUseCase(repository))

    private fun scheduleItem(
        animeId: Int,
        airedEpisodes: Int? = 1,
        availableEpisodeCount: Int? = null,
        releases: List<AnimeEpisodeRelease>,
    ) = AnimeScheduleItem(
        animeId = animeId,
        title = "Title $animeId",
        poster = null,
        nextDateEpochSeconds = null,
        airedEpisodes = airedEpisodes,
        totalEpisodes = 12,
        availableEpisodeCount = availableEpisodeCount,
        videoReleases = releases,
    )

    private fun release(animeId: Int, publishedAt: Long, episode: Int = 2) =
        AnimeEpisodeRelease(
            animeId = animeId,
            episodeNumber = episode,
            firstVideoAtSeconds = publishedAt,
            firstVideoId = animeId * 100 + episode,
            title = "Title $animeId",
        )

    private companion object {
        const val DAY = 24L * 60 * 60
        const val NOW = 1_791_000_000L
    }
}
