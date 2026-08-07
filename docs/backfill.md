# Backfill & sync design

Design for populating the DB from the Strava API after a cloud deploy, where
the DB starts empty (~3,000 activities to load) and Strava's rate limits
(600 requests / 15 min, 30,000 / day) mean this can't happen in one request
burst. Target deployment: Railway (app + managed Postgres), always-on
(no "Serverless" sleep), so a `@Scheduled` job is viable.

## The numbers

- **Base activity list**: 3,000 ÷ 100 per page ≈ **30 requests**. Cheap —
  fits in a single 15-min window.
- **Enrichment (kudos + comments)**: up to **6,000 requests** (1 per
  activity per type). This is the part that needs pacing across multiple
  15-min windows — realistically hours, well within the daily cap if spread
  out.

These are treated as two separate, independently resumable jobs rather than
one big loop.

## What's already in place

- `FetchService.fetchOlderActivities` derives its resume point from
  `RideActivityRepository.findOldestRideActivity()` — no separate cursor
  table needed for the backward walk.
- `RideActivity.id` is Strava's own activity ID, so `repository.save()` is
  an idempotent upsert. Re-fetching a page is wasteful but never corrupts
  data.
- `findPublicActivitiesWithMismatchedKudosCounts()` /
  `...CommentCounts()` already identify which activities still need
  enrichment.

## Gaps this design closes

1. No rate-limit awareness — `StravaService.fetchData` ignores Strava's
   response headers and swallows HTTP 429 into a silent `null`.
2. Throttling is a blind per-invocation count (`MAX_FETCHES = 5`), not a
   real budget check.
3. Nothing triggers repeat runs automatically — only manual `GET` calls to
   `FetchController`.
4. No cloud-compatible OAuth bootstrap — `StravaClient` reads the redirect
   URL from `System.in`, which has no equivalent on a deployed host.
5. `FetchController` takes `accessToken` as a URL path variable, which ends
   up in access logs on a cloud host.

## New classes

**`client.StravaRateLimiter`** *(singleton bean, in-memory)*
Tracks Strava's live usage from response headers; nothing to persist since
Strava reports authoritative current usage on every call.
```
- int shortTermUsage, shortTermLimit     // 15-min window
- int dailyUsage, dailyLimit
- Instant rateLimitedUntil               // set on 429, else null
+ void updateFromHeaders(HttpHeaders)
+ boolean hasBudgetFor(int requestCount)
+ boolean isExhausted()
+ void markRateLimited(Duration retryAfter)
```

**`exception.StravaRateLimitException`** *(unchecked)*
Thrown by `StravaService` on HTTP 429 instead of the current swallow-and-return-null.
```
- Duration retryAfter
```

**`model.SyncState`** *(JPA entity, single row — same pattern as `OAuthToken`)*
```
- long id
- boolean activityBackfillComplete
- Instant lastRunStartedAt, lastRunFinishedAt
- String lastRunOutcome   // COMPLETED | RATE_LIMITED | ERROR
- String lastError        // nullable
```

**`repository.SyncStateRepository`** extends `JpaRepository<SyncState, Long>`
Trivial — mirrors `OAuthTokenRepository`'s `findAll().stream().findFirst()` usage.

**`service.StravaSyncOrchestrator`**
The budgeted, phase-ordered entry point. Depends on `StravaOAuthService`,
`StravaRateLimiter`, `FetchService`, `SyncStateRepository`.
```
+ void runBudgetedSync()
- void ensureAthleteFetched(String token)
- void syncNewestActivities(String token)
- void continueActivityBackfill(String token)   // skipped once SyncState.activityBackfillComplete
- void enrichKudosAndComments(String token)
```
Each phase checks `rateLimiter.isExhausted()` before starting and bails
(recording `RATE_LIMITED`) rather than pushing into a 429. Phases run in
this order so the most recent data is always the first thing kept current
and the first thing enriched.

**`service.StravaSyncScheduler`**
```
@Scheduled(fixedDelay = ...)
+ void run()   // delegates to orchestrator.runBudgetedSync()
```
Spring's default scheduler thread is single-threaded, so `fixedDelay` alone
prevents overlapping runs — no separate lock needed.

**`controller.StravaOAuthCallbackController`** *(new — replaces the Scanner-based flow for cloud use)*
```
GET /oauth/authorize   → redirect to stravaOAuthService.getAuthorizationUrl()
GET /oauth/callback?code=...  → stravaOAuthService.getOAuthToken(code); store token
```
One-time step after each deploy (or whenever the refresh token is
invalidated); after that the scheduled job keeps the token current via the
existing `refreshToken` path.

**`controller.SyncStatusController`** *(observability, optional but recommended)*
```
GET /sync/status → SyncStatusDTO
```

**`dto.SyncStatusDTO`**
```
- long totalActivities
- LocalDateTime oldestActivityDate, newestActivityDate
- boolean activityBackfillComplete
- long pendingKudosCount, pendingCommentsCount
- int shortTermUsage, shortTermLimit, dailyUsage, dailyLimit
- Instant lastRunAt
- String lastRunOutcome
```

## Modified classes

**`service.StravaService`**
- `fetchData(...)` updates `StravaRateLimiter` from every response's
  headers, and throws `StravaRateLimitException` on 429 instead of logging
  and returning `null`.

**`service.FetchService`**
- Drop the hardcoded `MAX_FETCHES` constant.
- `fetchActivities(...)` checks `rateLimiter.hasBudgetFor(1)` before each
  page, and returns a result instead of `void`:
  ```
  record FetchResult(int fetchedCount, boolean stoppedOnBudget, boolean reachedEnd)
  ```
  (`reachedEnd` is what lets the orchestrator flip
  `SyncState.activityBackfillComplete`.)
- `fetchKudos`/`fetchComments` loop until `rateLimiter` budget runs low
  rather than a fixed count of 5.

**`repository.RideActivityRepository`**
- Add `ORDER BY r.startDateLocal DESC` to
  `findPublicActivitiesWithMismatchedKudosCounts()` /
  `...CommentCounts()` so enrichment does recent activities first.

**`controller.FetchController`** — retired
- Deleted. Superseded by `StravaSyncScheduler` (automatic runs) and
  `SyncStatusController` (visibility); its `accessToken`-in-URL endpoints
  had no place in the new design.

**`client.StravaClient`**
- Class annotated `@Profile("local")` — its Scanner-based interactive
  authorization flow only makes sense with a local terminal. Not wired up
  by default; `StravaFetcherApplication`'s startup `CommandLineRunner` that
  used to invoke it on every boot was removed in favor of
  `StravaSyncScheduler`.

**`StravaFetcherApplication`**
- Added `@EnableScheduling`; removed the `CommandLineRunner` bean.

## Environments: local vs. Railway

All environment-specific config is read from env vars in
`application.properties`, each with a `local`-friendly default baked in.
Nothing in the jar/build differs between environments; only these
variables change.

| Variable | Local (default in `application.properties`) | Railway (cloud) |
|---|---|---|
| `STRAVA_CLIENT_ID` | `14826` (default, not sensitive) | same, unless the Strava app is ever recreated |
| `STRAVA_CLIENT_SECRET` | baked-in default (kept as-is, per explicit decision — not rotated) | same value, or override in Railway Variables if rotated |
| `STRAVA_REDIRECT_URI` | `http://localhost:8080/oauth/callback` | `https://<railway-domain>/oauth/callback`, e.g. `https://strava-fetcher-production.up.railway.app/oauth/callback` |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/strava` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` (Railway variable reference to the Postgres service) |
| `DATABASE_USERNAME` | `jon` | `${{Postgres.PGUSER}}` |
| `DATABASE_PASSWORD` | baked-in default (local-only Postgres password) | `${{Postgres.PGPASSWORD}}` |
| `TOMCAT_PORT` | `8080` (default) | usually left unset — Railway's proxy targets the app's default port automatically |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` (Angular dev server) | frontend's real origin, e.g. its CloudFront domain — not the backend's own Railway domain |

Notes specific to each environment:

**Local**
- Needs a local Postgres instance reachable at `DATABASE_URL`, with the
  `strava` schema created manually first (Postgres doesn't auto-create
  schemas; `ddl-auto=update` only creates tables inside an existing one):
  `CREATE SCHEMA IF NOT EXISTS strava;`
- Run with `./mvnw spring-boot:run`, then visit
  `http://localhost:8080/oauth/authorize` once to connect Strava.
- `client.StravaClient`'s Scanner-based interactive OAuth flow is
  `@Profile("local")`-only; the web-based `/oauth/authorize` +
  `/oauth/callback` flow works in both environments and is the one actually
  used day-to-day.

**Railway (cloud)**
- App service and Postgres are separate Railway services in the same
  project; the `DATABASE_*` variables above use Railway's `${{Service.VAR}}`
  reference syntax to pull connection details from the Postgres service —
  set these on the **app service's** Variables tab, not the Postgres
  service's.
- Same manual schema-creation step as local, but run against the Railway
  DB (Railway's Data tab has a query console, or `railway connect postgres`).
- Leave the "Serverless" sleep option **off** on the app service — required
  for `StravaSyncScheduler`'s `@Scheduled` job to keep firing without an
  inbound request waking the app.
- Generate the app's public domain under Settings → Networking if not
  already done, then set `STRAVA_REDIRECT_URI` to match.
- **Strava's API application only supports one registered "Authorization
  Callback Domain" at a time** (Strava dashboard, not this app) — there's
  no way to register both `localhost` and the Railway domain
  simultaneously. Switching which environment you're testing OAuth against
  means swapping that one field at
  https://www.strava.com/settings/api. Everything else (tokens, synced
  data) is per-database, not per-domain, so this only matters for the
  `/oauth/authorize` step itself.
- After deploy, `GET /sync/status` reports `lastRunOutcome`
  (`NOT_AUTHORIZED` / `RATE_LIMITED` / `ERROR` / `COMPLETED`) and activity
  counts — the fastest way to confirm the scheduled sync is actually
  running against the cloud DB rather than debugging blind.

## Implementation status

Implemented. Decisions made along the way:

- `FetchController` retired entirely (deleted), not kept for manual
  debugging — `SyncStatusController` + the scheduler cover that need.
- Scheduler runs every 10 minutes (`StravaSyncScheduler`, `fixedDelay`).
- `strava.client.id` moved to `${STRAVA_CLIENT_ID:14826}` (kept the old
  value as a local default — not sensitive) and `strava.client.secret`
  moved to `${STRAVA_CLIENT_SECRET}` with **no default**, so the app fails
  fast at startup if it's missing rather than silently misconfiguring.

## Follow-ups not yet done

- **Rotate the Strava client secret.** Removing it from
  `application.properties` doesn't remove it from git history — the old
  value is still recoverable there. Generate a new secret in Strava's API
  settings and set it via `STRAVA_CLIENT_SECRET` everywhere (local shell
  env, Railway).
- **Register both callback URLs with Strava** — `http://localhost:8080/oauth/callback`
  for local dev and the Railway domain's `/oauth/callback` for cloud —
  Strava's authorization flow only redirects to URLs registered on the API
  application.
- `sync_state` table is picked up automatically by
  `spring.jpa.hibernate.ddl-auto=update` — no manual migration needed, but
  worth confirming that's still the desired setting once this reaches
  production data.
- No automated tests were added for the new budget/backoff logic
  (`src/test` is currently empty in this repo).