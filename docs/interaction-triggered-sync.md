# Interaction-triggered sync design

Replaces the fixed 15-minute polling clock as the *primary* trigger for
`StravaSyncOrchestrator.runBudgetedSync()` with a signal derived from actual
app usage: a hit on this app's own API. Complements, but doesn't replace,
the existing sweep-based mechanisms in
[`reconciliation.md`](./reconciliation.md) and
[`backfill.md`](./backfill.md), and supersedes the webhook design in
[`webhooks.md`](./webhooks.md) as the chosen approach for now.

## Why

This is a single-user hobby project. The old scheduler ran
`runBudgetedSync()` on a blind clock regardless of whether anyone was
looking — ~96 ticks/day, most of which discover nothing changed. The
webhook design (`webhooks.md`) would fix that with real push events, but
needs a public callback URL, a Strava-side subscription/handshake, a new
`WebhookEvent` table, and an event-processing pipeline — real
infrastructure for a project with one user.

Instead: use a request to this app's API as the "someone's here" signal.
`runBudgetedSync()` already does the hard part — phase-gating via
`SyncState` timestamps, rate-limiter checks, resumable cursors — so this
only changes what triggers it, not its internals.

## Design

**`service.SyncTriggerService`** — single entry point both trigger sources
call:

- A dedicated single-thread `ExecutorService` that `runBudgetedSync()` is
  always invoked through, so the fallback scheduler and every
  request-triggered call can never race each other on `SyncState`.
- An `AtomicBoolean` in-flight guard: if a submission is already running,
  new `trigger()` calls no-op. Stops a burst of simultaneous frontend calls
  (e.g. one page load firing several API requests) from queuing up
  redundant runs.
- A 5-minute in-memory cooldown, independent of `SyncState`'s per-phase
  timestamps — those already gate whether an individual *phase* has work to
  do; this just gates how often we even bother asking.

**`config.SyncTriggerInterceptor`** — a `HandlerInterceptor` registered on
`/**` in `WebConfig`. Calls `SyncTriggerService.trigger()` in `preHandle`
and always returns `true`. Fire-and-forget: the response never waits on a
live Strava call, even when a sync is due. A page loaded right after
finishing a ride may still show stale data until the next request/refresh
picks up the synced result — accepted tradeoff for keeping every response
fast and the implementation simple.

**`service.StravaSyncScheduler`** — kept as a fallback, not removed. Still
needed so backfill/reconciliation/kudos-comments progress (and a missed
activity sync) even during stretches where nobody opens the app. Interval
lengthened from 15 minutes to 3 hours, and it now calls
`SyncTriggerService.trigger()` instead of the orchestrator directly, so it
goes through the same serialized executor as request-triggered runs.

No `SyncState` schema changes — its existing per-phase timestamps keep
doing exactly what they already did.

## Alternatives considered

- **Synchronous quick check on activity reads** — block `GET
  /activities/**` briefly (bounded to one Strava call, the newest-activities
  phase only) so a fresh page load could show a just-finished ride
  immediately. Rejected for now in favor of simplicity and to avoid adding
  request-latency risk tied to Strava's response time; every endpoint stays
  fast regardless of sync state.
- **Hybrid: background trigger + explicit `/sync/refresh-now` endpoint** —
  keep normal requests fire-and-forget, add a dedicated endpoint for the
  frontend to call explicitly (pull-to-refresh, a "check now" button) that
  does the synchronous check. Reasonable follow-up if "I want to confirm my
  ride landed right now" becomes an actual annoyance in practice; would
  need a small `strava-ui` change to call it.
- **Webhooks** (`webhooks.md`) — still the only approach that gives
  real-time correctness independent of whether anyone's looking. Left
  shelved; revisit if interaction-triggered sync's staleness (bounded by
  visit cadence, not wall-clock time) ever actually matters in practice.

## Open items / follow-ups

- Cooldown (5 min) and fallback interval (3 hours) are both judgment calls,
  not derived from any hard requirement — tune after observing real usage
  and Strava rate-limit headroom (`GET /sync/status`).
- No automated tests, consistent with `backfill.md`/`reconciliation.md`'s
  status (`src/test` is currently empty in this repo).
