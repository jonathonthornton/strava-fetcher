# Reconciliation sweep design

Design for keeping the DB honest against two kinds of drift that the
existing sync (see [`backfill.md`](./backfill.md)) doesn't cover:

1. **Edits on Strava that land outside the current sync window.**
   `RideActivity.id` is Strava's own activity ID, so `repository.save()` is
   an upsert — a name or gear change *is* picked up for free whenever an
   activity happens to be re-fetched. But the regular sync only re-fetches
   the rolling 6-month "recent" window, plus whatever the one-time backward
   backfill hasn't passed yet. Once backfill reaches the start of the
   account's history it stops forever — an edit to a two-year-old ride's
   name or bike never syncs again.
2. **Activities deleted on Strava.** Nothing currently detects this. The
   activity-list endpoint simply omits deleted activities; there's no
   diffing between "what Strava returns" and "what's in the DB," and no
   delete path anywhere in the sync code.

Both are low-urgency ("doesn't need to happen often," per the driving
request), so this is designed as an infrequent (monthly), fully-resumable
sweep rather than real-time push updates — Strava does offer a webhook for
create/update/delete events, but that requires a public callback endpoint
and subscription lifecycle management this app doesn't have yet. A
periodic sweep reuses 100% of the existing fetch/save/rate-limit machinery
and gets both problems for the cost of walking the activity list end to
end.

## The numbers

Same list endpoint as backfill, no window restriction: for ~3,000
activities that's ~30 requests for a full pass. Trivial against Strava's
limits (600/15min, 30,000/day) even run back-to-back with the regular
sync's own usage.

## Design: a fifth phase on the existing orchestrator

Rather than a separate scheduler competing for rate-limit budget, the
sweep is folded into `StravaSyncOrchestrator.runBudgetedSync()` as a new
phase that runs **last**, after `enrichKudosAndComments`. This gets
"regular sync takes priority" for free — the existing phase order already
means each phase only spends whatever budget the ones before it didn't
use, and every phase already bails via `rateLimiter.isExhausted()`. No new
coordination logic needed.

The phase itself does very little on most ticks:

- If no sweep is in progress and the last completed one was ≥30 days ago
  (or never happened), start one: stamp `reconciliationSweepStartedAt`,
  reset the cursor.
- If a sweep is in progress and budget remains, fetch the next page
  (walking `before` backwards from the cursor, no lower bound — full
  history, not just back to the DB's current oldest row) and save each
  activity, which also stamps `lastSyncedAt` on every touched row.
- If the page fetch comes back empty, the sweep is done: run the deletion
  pass (below), stamp `reconciliationLastCompletedAt`, clear the
  in-progress fields.

Because each tick just continues from a persisted cursor, an interrupted
sweep (rate-limited, app restart, whatever) resumes cleanly next tick —
same pattern as the existing backfill.

## Deletion pass

Only runs immediately after a sweep reaches the end of Strava's history
**cleanly** — i.e., the `reconciliationSweepStartedAt` → completion was one
continuous logical sweep with no reset in between. This matters: a sweep
that got interrupted and *restarted from scratch* must not trigger
deletions based on a partial pass, or a mid-sweep rate-limit hiccup would
wrongly delete every activity the sweep hadn't reached yet. Resuming
mid-sweep is fine (the cursor picks up where it left off, same
`reconciliationSweepStartedAt`); only a full restart of the sweep counts as
"partial" for this purpose.

Once a sweep completes cleanly:

```
activities = rideActivityRepository.findStaleActivities(sweepStartedAt)   // lastSyncedAt IS NULL OR < sweepStartedAt
for each: log.info("Deleting activity no longer on Strava: id={} name={} date={}", ...)
ids = activities.map(RideActivity::getId)
kudosRepository.deleteByActivityIdIn(ids)
commentRepository.deleteByActivityIdIn(ids)
rideActivityRepository.deleteAll(activities)
```

Two adjustments from the original sketch, both in `FetchService.deleteStaleActivities`:

- The query is `lastSyncedAt IS NULL OR lastSyncedAt < cutoff`, not just
  `< cutoff`. SQL comparisons against `NULL` evaluate to unknown (i.e.
  excluded), so a plain `<` would silently skip every pre-existing prod row
  that predates this feature and no longer exists on Strava — exactly the
  case the "First-run note" below depends on catching. `IS NULL` closes
  that gap.
- No `Athlete` parameter — dropped since nothing else in this codebase
  scopes by athlete either (`SyncState` is a singleton row, there's a
  single OAuth token). Adding athlete-scoping here would be the only place
  in the app pretending to support multiple athletes.

`Kudos` (composite PK of `activity_id` + `follower_id`) and `Comment`
(`activity_id` as a plain column) are both tied to an activity by
`activity_id`, but neither is mapped in JPA as a `@ManyToOne`/`@JoinColumn`
relation on `RideActivity` — unlike `ActivityMap`, which cascades
automatically via `CascadeType.ALL`. That means these two need an explicit
delete step; nothing cascades them for free. This matters regardless of
whether a DB-level FK constraint also exists on these columns (plausible,
since `ddl-auto=update` never drops constraints — if these entities once
had a real `@ManyToOne` to `RideActivity` before being simplified to plain
`activityId` longs, an old FK could still be sitting in the live schema):
skipping this step either throws `DataIntegrityViolationException` on the
`RideActivity` delete (if a constraint exists) or leaves orphaned rows
that corrupt the existing `findTopKudosers`/`findTopCommenters`-style
reports, which already join `Kudos`/`Comment` to `RideActivity` on
`activity_id` (`KudosRepository.java:16`, `CommentRepository.java:17`).
Deleting all three in one transaction avoids both outcomes.

Any row not touched during the just-completed full pass wasn't mentioned
by Strava anywhere in that pass — i.e., deleted on their side. Hard delete
(not soft), per explicit decision — nothing else in this schema models a
"deleted" state, and adding one would mean updating every existing query
and report to filter it out for one feature's benefit. The logged
id/name/date before each delete is the only audit trail, since there's no
undo.

**First-run note**: prod already has data, and some of it may already
correspond to activities deleted on Strava before this feature existed.
The first sweep to ever complete will delete those in one batch — correct
behavior, but worth knowing in advance rather than being surprised by it
in the logs.

## New / modified fields

**`model.SyncState`** (existing single-row table)
```
+ Instant reconciliationSweepStartedAt    // null when no sweep in progress
+ Instant reconciliationCursorBefore      // paging cursor; null = start from now
+ Instant reconciliationLastCompletedAt   // null until the first full sweep finishes
```

**`model.RideActivity`**
```
+ Instant lastSyncedAt   // @Column(name = "last_synced_at")
```
Stamped explicitly in `FetchService`'s single shared save call site
(`fetchActivities`), not via a `@PrePersist`/`@PreUpdate` lifecycle
callback as originally planned here. That approach turned out to be a real
bug: Hibernate's dirty-checking on `merge()` compares field values against
the loaded snapshot *before* firing `@PreUpdate`, and skips the UPDATE
(and the callback) entirely when nothing else on the entity changed —
which is the common case of re-fetching an activity that's identical to
what's already stored. Under that approach, an unchanged-but-still-valid
activity would never get its timestamp bumped and would eventually look
stale to the deletion pass. Setting `lastSyncedAt = Instant.now()` in Java
right before `save()` sidesteps this: the new timestamp is virtually
always different from what's persisted, so the entity is reliably dirty
regardless of whether any other field changed. Every activity-saving path
(regular sync, backfill, reconciliation) already funnels through this one
method, so one call site covers all of them.

## New / modified classes

**`service.StravaSyncOrchestrator`** (modified)
```
+ void continueReconciliationSweep(String token, SyncState syncState)   // new 5th phase, runs last
```
Called from `runBudgetedSync()` immediately after `enrichKudosAndComments`.

**`service.FetchService`** (modified)
```
+ FetchResult fetchActivitiesForReconciliation(String token, Instant before)
```
Same page-fetch-and-save mechanics as `fetchOlderActivities`, but cursor
comes from `SyncState.reconciliationCursorBefore` rather than being
derived from the DB's current oldest row — the sweep needs to walk the
*entire* history independent of what backfill has already covered.

**`repository.RideActivityRepository`** (modified)
```
+ List<RideActivity> findStaleActivities(Instant cutoff)   // lastSyncedAt IS NULL OR < cutoff
```
Used by the deletion pass; returns full entities (not just IDs) so each
deletion can be logged with enough detail to be useful.

**`repository.KudosRepository`** (modified)
```
+ void deleteByActivityIdIn(Collection<Long> activityIds)
```

**`repository.CommentRepository`** (modified)
```
+ void deleteByActivityIdIn(Collection<Long> activityIds)
```
Both called before `rideActivityRepository.deleteAll(...)` in the deletion
pass, in the same transaction — see "Deletion pass" above.

**`controller.ReconciliationController`** *(new — manual trigger)*
```
POST /admin/reconciliation/trigger
```
Sets `reconciliationSweepStartedAt = now()` and resets the cursor if no
sweep is already in progress (no-op otherwise); the next orchestrator
tick(s) pick it up and run it to completion automatically, same as any
other sweep. Exists so the feature can be verified right after deploy
instead of waiting up to a month for the first natural trigger.

**`dto.SyncStatusDTO`** (modified) — add `reconciliationInProgress`,
`reconciliationLastCompletedAt` so `GET /sync/status` shows sweep state
alongside the existing backfill/rate-limit info.

## Schema migration

`spring.jpa.hibernate.ddl-auto=update` (no Flyway/Liquibase in this
project) will auto-add `ride_activity.last_synced_at` and the new
`sync_state` columns on next startup — both are nullable, purely additive,
so no manual `ALTER TABLE` is needed. Existing rows get `last_synced_at =
NULL` until the first sweep touches them, which is fine since the deletion
pass never runs until a sweep has fully completed and touched everything
Strava currently reports.

Recommended as a safety net regardless: take a prod DB backup before this
deploy. `ddl-auto=update` has no rollback of its own if Hibernate infers
something unexpected.

## Decisions made

- **Hard delete**, not soft — see rationale above.
- **Monthly** sweep cadence (`reconciliationLastCompletedAt` older than 30
  days triggers the next one).
- **Regular sync takes priority** for rate-limit budget — achieved
  structurally by making the sweep the last phase of the existing
  orchestrator run, not a separately scheduled competitor.
- **Manual trigger endpoint added**, for post-deploy verification and
  occasional on-demand cleanup, rather than cron-only.
- **`POST /admin/reconciliation/trigger` left unauthenticated.** No admin
  auth layer exists in this app (OAuth is between this app and Strava, not
  end-user auth), and this is a single-user hobby project — worst case of
  it being open is an extra unnecessary sweep, not data loss. Not worth
  building an auth layer for.

## Open items / follow-ups

- No automated tests planned yet (`src/test` is currently empty in this
  repo, per `backfill.md`).

## Implementation status

Implemented. All new/modified classes above match what's in the codebase,
with the two corrections noted inline (`lastSyncedAt` stamped explicitly
rather than via lifecycle callback; `findStaleActivities` treats `NULL` as
stale and isn't athlete-scoped). Schema changes apply automatically via
`ddl-auto=update` on next startup — take a prod DB backup first regardless
(see "Schema migration" above).

No automated tests were added (`src/test` is empty in this repo, same as
`backfill.md`'s status). The sweep's cadence/completion logic in
particular — resume-after-interruption, the "only delete after a clean
full pass" guard — would benefit most from tests before this has run
against prod data for real, since it's the part with no easy way to
observe correctness other than trusting the logs.
