# Webhook-driven sync design

> **Status:** shelved in favor of the lighter approach in
> [`interaction-triggered-sync.md`](./interaction-triggered-sync.md), which
> was implemented instead. Revisit this doc if that approach's staleness
> (bounded by visit cadence, not wall-clock time) becomes a real problem.

Design for replacing polling-based activity discovery with Strava's push
webhook (https://developers.strava.com/docs/webhooks/), reducing scheduled
API usage. Complements, but doesn't replace, the existing sweep-based
mechanisms in [`reconciliation.md`](./reconciliation.md) and
[`backfill.md`](./backfill.md).

## Why

The regular sync (`StravaSyncOrchestrator.syncNewestActivities`) currently
polls "what's new since last sync" every 10 minutes
(`StravaSyncScheduler.TEN_MINUTES_MS`), regardless of whether anything
actually changed. Strava's webhook pushes an event the moment an activity
is created, updated, or deleted, so the app can react instead of asking.
This doesn't eliminate polling entirely — kudos/comments and athlete
deauthorization have no webhook events, and webhook delivery is
best-effort (3 retries, no guaranteed delivery), so a lower-frequency
fallback sweep stays in place as a safety net, same philosophy as the
reconciliation sweep.

Explicit scope decisions driving this design, per the discussion that
produced it:

- Kudos/comments enrichment: fine to run **monthly** rather than every
  tick — no webhook event exists for these anyway (see "What webhooks
  don't cover" below).
- The reconciliation sweep (30-day full-history walk, deletion detection)
  stays exactly as it is — this design doesn't touch it.

## What webhooks cover vs. don't

| Phase | Webhook event | Outcome |
|---|---|---|
| `syncNewestActivities` (new/updated activities) | `activity` / `create`, `update` | Replaced by push; polling becomes a low-frequency fallback only |
| Deletion detection (currently only via reconciliation sweep) | `activity` / `delete` | Handled immediately by push; reconciliation sweep still runs monthly as backstop for missed deliveries |
| `ensureAthleteFetched` (profile fields: stats, bikes, etc.) | *(none — only deauth is pushed)* | Stays a daily poll, unchanged |
| Deauthorization | `athlete` / `update`, `updates.authorized == "false"` | New: react immediately instead of discovering it via a failed token refresh |
| `enrichKudosAndComments` | *(none — not a supported event type)* | Stays polling, cadence reduced to monthly |
| `continueActivityBackfill` (one-time historical walk) | *(none — webhooks only cover events from subscription time forward)* | Unchanged |

## The numbers

Current: ~144 scheduler ticks/day, each potentially issuing at least one
`/athlete/activities` call to check for new activity even when nothing
changed. After this change, that call only fires when a webhook event
actually arrives (real usage, not calendar-driven), plus one lower-frequency
fallback poll as a backstop. Kudos/comments enrichment drops from up to
144 opportunities/day to 1/month. Both changes target the phases that
were running on a fixed clock regardless of whether there was anything to
do — the same category of waste the `9a41e1a` incremental-fetch change
already addressed for the activity window itself.

## Design: webhook receiver + a lighter-weight scheduler

### 1. Strava-side subscription (one-time, not app code)

Requires `activity:read_all` scope (already requested — see
`StravaOAuthService.AUTHORIZATION_SCOPE`). Steps:

1. Add `strava.webhook.verify-token` and `strava.webhook.callback-url` to
   `application.properties`, same `${ENV_VAR:default}` pattern as
   `strava.client.id`/`strava.client.secret`.
2. Deploy the receiver (section 2) first — Strava validates the callback
   URL synchronously when the subscription is created.
3. One-time `curl POST https://www.strava.com/api/v3/push_subscriptions`
   with `client_id`, `client_secret`, `callback_url`, `verify_token`. Not
   app code — there's only one subscription per app, ever.
4. Strava GETs the callback with `hub.challenge` / `hub.verify_token` /
   `hub.mode`; the receiver must echo `{"hub.challenge": "<value>"}` within
   2 seconds after checking `hub.verify_token` matches config.
5. Record the returned `subscription_id` for sanity-checking incoming
   events (informational only — only one subscription can be active).

### 2. Webhook receiver

**`controller.WebhookController`** *(new)*, same style as
`StravaOAuthCallbackController`:
```
GET  /webhook/strava   // handshake: verify hub.verify_token, echo hub.challenge
POST /webhook/strava   // event receiver
```
The POST handler must return `200` within 2 seconds, so it must not call
the Strava API inline. It validates the payload, persists it, and returns
immediately; processing happens out-of-band (section 3).

**`model.WebhookEvent`** *(new table)*
```
id              (generated)
object_id       long     // activity or athlete id
object_type     String   // "activity" | "athlete"
aspect_type     String   // "create" | "update" | "delete"
owner_id        long     // athlete id the event belongs to
event_time      Instant
received_at     Instant
processed_at    Instant  // null until processed
```
A durable log rather than in-memory queue — keeps the 2-second response
window trivial and lets processing be retried independently, consistent
with this app's existing preference for DB-backed resumability
(`SyncState`'s cursor fields) over in-memory state.

**`repository.WebhookEventRepository`** *(new)*
```
List<WebhookEvent> findByProcessedAtIsNull()
```

### 3. Event processing

**`service.WebhookEventProcessor`** *(new)*, invoked by a new
`@Scheduled` tick (short interval, e.g. every 30 seconds — cheap since it's
a DB read, not a Strava call, when there's nothing pending):

- `activity` + `create`/`update` → fetch the single activity and upsert.
  Requires a new single-activity fetch method, since `StravaService`
  currently only exposes the paginated list endpoint:
  ```
  StravaService.getActivity(String accessToken, long activityId)   // GET /activities/{id}
  ```
  Upserts via the same save path `FetchService.fetchActivities` already
  uses (`RideActivity.id` = Strava's id, so `repository.save()` is an
  upsert already).
- `activity` + `delete` → delete immediately. Extract the shared
  delete-by-id logic (activity + cascaded kudos/comments) out of
  `FetchService.deleteStaleActivities` into a reusable
  `FetchService.deleteActivity(long activityId)` so both the sweep and the
  webhook path share one implementation.
- `athlete` + `update` with `updates.authorized == "false"` → flag
  `SyncState` (e.g. `authorizationRevokedAt`) so the scheduler stops
  attempting syncs and logs a clear "reauthorize via /oauth/authorize"
  message instead of repeatedly failing token refresh.
- Every Strava call in this path still goes through
  `rateLimiter.hasBudgetFor(1)` first, same as every other fetch path —
  webhooks change *when* a fetch happens, not whether it still costs
  budget.
- **`owner_id` check**: before acting, compare `event.ownerId` against the
  single stored `Athlete.id` (`athleteRepository.findAll().findFirst()`,
  same singleton-row pattern as `SyncState`/`OAuthToken`) and discard
  events that don't match. Strava's webhook spec has no signature
  verification beyond the `verify_token` handshake, so this is the only
  guard against a forged POST to a discovered callback URL — worth doing
  even though a single-user hobby project has little at stake beyond a
  wasted fetch.
- Mark `processed_at` on success. Duplicate delivery of the same event is
  harmless (upsert / idempotent delete), so no additional dedupe logic
  needed beyond not endlessly reprocessing.

### 4. Orchestrator changes

**`model.SyncState`** — add:
```
+ Instant lastKudosCommentsSyncAt
```
**`service.StravaSyncOrchestrator`** — gate `enrichKudosAndComments` the
same way `ensureAthleteFetched` already gates on
`ATHLETE_SYNC_INTERVAL`:
```
private static final Duration KUDOS_COMMENTS_SYNC_INTERVAL = Duration.ofDays(30);
```
Skip the phase unless `lastKudosCommentsSyncAt` is null or older than the
interval; stamp it after running, same pattern as
`ensureAthleteFetched`/`lastAthleteSyncAt`.

`continueReconciliationSweep` is untouched — stays the 30-day safety net
it already is.

**`service.StravaSyncScheduler`** — `syncNewestActivities` no longer needs
to run every 10 minutes now that webhooks handle real-time create/update;
it becomes a fallback for missed webhook deliveries. Lengthen
`TEN_MINUTES_MS` (misnamed after this change) to something like an hour.
Backfill continuation and the reconciliation/kudos-comments due-checks are
cheap no-ops on ticks where they're not due, so a longer interval doesn't
delay them meaningfully.

## Decisions made

- **Kudos/comments: monthly**, gated the same way athlete sync already is
  — explicit tradeoff accepted (kudos/comment counts can be up to a month
  stale) in exchange for cutting that phase's request volume by ~30x.
- **Reconciliation sweep: unchanged.** Not folded into the webhook design
  — it already serves as the backstop for webhook delivery failures, so
  touching its cadence isn't needed here.
- **Fallback polling kept, not removed.** Webhook delivery is best-effort
  (3 retries, no guarantee); a lower-frequency `syncNewestActivities` stays
  as the recovery path rather than relying on push exclusively.
- **`WebhookEvent` as a durable table, not an in-memory queue** — matches
  this app's existing resumability pattern (`SyncState` cursors) and keeps
  the POST handler's 2-second budget trivially safe.
- **`owner_id` is the only forgery guard** — acceptable for a single-user
  hobby project per the same reasoning `reconciliation.md` used for
  leaving `/admin/reconciliation/trigger` unauthenticated.

## Open items / follow-ups

- Exact fallback interval for `syncNewestActivities` (proposed: 1 hour) is
  a judgment call, not a hard requirement from Strava's docs — adjust once
  real webhook reliability is observed.
- No automated tests planned yet, consistent with `backfill.md` and
  `reconciliation.md`'s status (`src/test` is currently empty in this
  repo).
- `server.forward-headers-strategy=native` (Railway terminates TLS
  upstream) should already make the callback URL resolve to `https://`
  correctly, same as the existing OAuth redirect — worth confirming during
  the handshake step rather than assuming.

## Implementation status

Shelved. The kudos/comments monthly cadence (`SyncState.lastKudosCommentsSyncAt`,
`StravaSyncOrchestrator.KUDOS_COMMENTS_SYNC_INTERVAL`) was implemented
directly, independent of webhooks — see "Decisions made" above, that part
never depended on the receiver/subscription machinery. The webhook
receiver, event processing, and the `syncNewestActivities` fallback-cadence
change described above are not implemented; this doc is left as a plan to
pick back up later.
