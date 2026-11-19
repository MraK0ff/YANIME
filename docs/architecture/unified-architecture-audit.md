# YANIME Unified Architecture — audit and migration plan

> Status: initial repository-wide inventory and focused architecture audit. This document records verified findings and the safe migration sequence; it is not a claim that every call site or runtime path has already been audited.

## 1. Repository shape

The audited tree contains approximately 4,479 files. The project is already modular:

- `core`: common, model, storage, network, preferences, navigation, MVI, design system, errors, testing and platform support.
- `feature`: independent feature families with API/domain/data/presentation and mobile/TV UI modules where applicable.
- `source`: provider-specific source modules, intentionally isolated behind source adapters.
- `app`: composition root, Android workers, notification scheduling, outbox scheduling and platform integration.

The project already follows a layered Gradle structure. The target is to strengthen existing boundaries, not introduce a replacement architecture or collapse feature modules.

## 2. Verified existing shared infrastructure

### Design system

`core/designsystem` already contains shared foundations that should be preferred over feature-local copies:

- Mobile poster/title cards: `MobilePosterCard`, `MobileTitleRowCard`, `MobileProgressMediaCard`.
- TV poster/title cards: `TvTitleCard`, `TvTitleListCard`, `TvTitleRowCard`, `TvProgressMediaCard`.
- Shared list-status, rating, release and source badges.
- Mobile and TV settings rows, tabs, loading/empty/error/offline states.
- Shared dialog and bottom-sheet hosts.
- Theme, semantic colors, shapes, spacing, dimensions and motion tokens.
- TV focus primitives and mobile input/press primitives.

The mobile and TV components should share tokens, models and semantics, but not be forced into one layout or interaction implementation.

### Local persistence and synchronization

`core/storage` already owns Room storage abstractions and the application database. It includes dedicated storage contracts for anime metadata, home feed, schedule, library, watch progress, account caches and other feature data.

The database already has an explicit migration chain through schema 74. Any new persistent release model must be introduced through a forward migration and migration tests; destructive recreation is not acceptable.

The project also has a durable pending-mutation outbox:
- `PendingMutationOutbox` and its Room DAO/store.
- Platform scheduler and worker in `app`.
- Online flush runner and tests.

This is an existing pattern to extend for retryable writes where appropriate, not a reason to add another generic queue.

## 3. Confirmed duplication / consistency risks

### Release facts are centralized; Home retains a presentation projection

The canonical source is now Room `episode_release_history`, keyed by anime + episode + normalized player + normalized dubbing. It stores source-aware publication observations and is exposed through `AnimeScheduleRepository.getEpisodeReleaseHistory()`. The old Schedule JSON document (`yummyanime:episode-release-history:v1`) is read-only and imported into Room when the new table is empty.

Home no longer runs its own `YaniNewReleaseTracker` or independently decides whether a new episode appeared. It derives its card list from the canonical release facts through `projectHomeNewReleases`. The Room `home_new_release_history` table remains as a Home presentation projection cache for card order and metadata; it is not authoritative for episode/source facts.

This preserves the semantic distinction between an episode and each dubbing/player publication. The Home projection has unit tests for baseline creation, new-episode promotion, alternate-dub stability, legacy zero-episode rows and aired-count filtering.

### Publication ingestion still has two polling entry points

Home maps its fetched Yani feed to `AnimeEpisodeRelease` and sends observations to the canonical schedule repository. Schedule also polls the same feed on its own refresh timer. Both paths converge on the same idempotent Room store, so they no longer create divergent histories, but duplicate network polling is still possible. A later slice should move polling ownership to one ingestion coordinator and make both features consume it.

### UI reuse is partly implemented, not absent

`HomeItemCard` is already a feature-specific adapter around `MobilePosterCard`, adding Home-specific metadata and year overlay. This is a good composition pattern and should be retained.

Other feature-local cards need call-site inspection before consolidation. For example, TV library uses `TvTitleListRow`, while poster cards use `TvTitleCard`; these are different presentation semantics and should not be merged merely because both represent anime. The audit should prioritize genuinely duplicated poster/title presentations and repeated settings/status rows.

### Shared state already exists for important user data

- `WatchProgressStorage` is the local authority for episode-level watch progress and exposes reactive flows.
- `AnimeStorage` caches details/videos and exposes available episode counts and server watch-progress snapshots.
- `LibraryStorage` owns local library/favorite state and exposes flows.
- Feature repositories add source-specific orchestration and mapping.

The next audit pass must trace all consumers and identify places that bypass these contracts or independently reconstruct the same business rules.

### Source modules should remain adapters

The `source/*` modules model provider-specific release/stream DTOs and parsing. Their provider-specific types are not automatically duplicates: different providers expose different capabilities and identifiers. Normalize them at a shared boundary only when the semantics match, retaining source IDs and provenance.

### Community cloud database is not yet verified as an in-repository backend

The audited repository tree contains local Room storage, a pending-mutation outbox, account/library sync code and community-related UI/settings references. The inspected tree did not reveal a clearly defined Community Database service contract or server implementation. Do not invent a backend URL, credentials, schema or provider. Before cloud integration, identify the previously planned service, its owner/repository, API contract, authentication and privacy rules. Until then, local persistence remains the offline source of truth and cloud sync is an explicit integration gap.

## 4. Target boundaries

Keep these responsibilities distinct:

- **Canonical domain entities**: anime identity, episode identity, source/player, dubbing, episode release, dubbing release, video availability and prediction.
- **Source adapters**: provider DTO parsing, provider identifiers and source-specific quirks.
- **Ingestion coordinator**: polling/scheduling, validation, normalization and deduplication.
- **Release repository**: canonical persisted release facts and reactive observation/query APIs.
- **Personalization**: subscriptions, watch progress, library membership and notification preferences applied to shared facts; never mutate global release facts.
- **Feature projections**: Home “New episodes”, “My new episodes”, Schedule, details and notifications derive their own presentation/query from shared release facts.
- **Presentation**: mobile and TV render their own interaction patterns over shared domain/UI state contracts.
- **Cloud sync**: a transport/sync adapter over explicitly defined shared facts, with idempotency and conflict policy; personal data remains separately scoped.

Avoid a giant `AnimeRepository` or universal UI card. Prefer focused contracts and small composable variants.

## 5. Migration sequence

1. Complete the module/call-site inventory, including repository dependencies, DTO/domain/storage models, UI card usages, network clients and worker schedules.
2. Define canonical identity keys and release event semantics. Episode identity must not include dubbing/player; dubbing-release identity must.
3. [x] Introduce source-aware canonical Room release history and import the old Schedule JSON document as a read-only migration source.
4. [x] Replace Home's independent episode tracker with a projection derived from canonical release facts; retain only Home card-order/presentation metadata as a cache.
5. Centralize feed polling in one ingestion coordinator and remove duplicate Home/Schedule polling entry points.
6. Make “My new episodes”, details and notifications consume the shared release facts. Keep personalized filtering separate.
7. Verify the Community Database contract and implement cloud sync only after its real API and security model are known.
8. Consolidate genuinely duplicated UI components by migrating call sites to existing design-system primitives; add typed slots/variants only where the existing primitive lacks a real semantic capability.
9. Consolidate duplicated business rules and mapping functions after usage tracing.
10. Add migration, idempotency, cross-feature integration and mobile/TV parity tests.
11. Remove superseded implementations after all consumers have moved and CI confirms the result.

## 6. Non-goals and safety rails

- No mass rename or one-shot rewrite.
- No single universal component with many unrelated Boolean flags.
- No merging mobile and TV interaction models.
- No matching anime solely by title.
- No mixing episode releases with individual dubbing releases.
- No cloud service or database invented without a verified contract.
- No deletion of legacy storage before migration/parity tests.
- No claim of full audit completion based solely on path inventory.

## 7. First implementation slice

The first code migration should target release-history ownership because the split is directly verified and affects Home, Schedule, notifications and prediction. The first slice must:
- define stable episode and dubbing-release identities;
- provide one ingestion/persistence contract;
- preserve current Home card ordering and Schedule history;
- migrate existing local records safely;
- keep the existing prediction algorithm as a consumer of canonical observations;
- add deduplication and migration tests.

UI card consolidation follows the data boundary work so that screens receive consistent models before their presentation adapters are simplified.
