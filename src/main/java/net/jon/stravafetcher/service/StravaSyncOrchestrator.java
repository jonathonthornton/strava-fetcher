package net.jon.stravafetcher.service;

import net.jon.stravafetcher.controller.StravaRateLimiter;
import net.jon.stravafetcher.exception.StravaRateLimitException;
import net.jon.stravafetcher.model.OAuthToken;
import net.jon.stravafetcher.model.SyncState;
import net.jon.stravafetcher.repository.SyncStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Runs one budgeted pass of the Strava sync: refresh the newest activities,
 * continue the backward backfill if it isn't finished, spend whatever
 * rate-limit budget is left on kudos/comments enrichment, then continue the
 * (infrequent) reconciliation sweep. Each phase checks the shared
 * {@link StravaRateLimiter} before doing any work, so a single run stops
 * cleanly instead of tripping a 429; the next scheduled run picks up
 * wherever this one left off.
 */
@Service
public class StravaSyncOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(StravaSyncOrchestrator.class);
    private static final Duration RECONCILIATION_INTERVAL = Duration.ofDays(30);
    private static final Duration ATHLETE_SYNC_INTERVAL = Duration.ofDays(1);
    private static final Duration KUDOS_COMMENTS_SYNC_INTERVAL = Duration.ofDays(30);

    private final StravaOAuthService stravaOAuthService;
    private final FetchService fetchService;
    private final StravaRateLimiter rateLimiter;
    private final SyncStateRepository syncStateRepository;

    public StravaSyncOrchestrator(
            StravaOAuthService stravaOAuthService,
            FetchService fetchService,
            StravaRateLimiter rateLimiter,
            SyncStateRepository syncStateRepository) {
        this.stravaOAuthService = stravaOAuthService;
        this.fetchService = fetchService;
        this.rateLimiter = rateLimiter;
        this.syncStateRepository = syncStateRepository;
    }

    public void runBudgetedSync() {
        SyncState syncState = syncStateRepository.findAll().stream().findFirst().orElseGet(SyncState::new);
        syncState.setLastRunStartedAt(Instant.now());

        try {
            Optional<String> accessToken = getValidAccessToken();
            if (accessToken.isEmpty()) {
                log.info("No usable Strava authorization on file; skipping sync run. Visit /oauth/authorize to (re)connect.");
                syncState.setLastRunOutcome("NOT_AUTHORIZED");
                syncState.setLastError(null);
                return;
            }

            String token = accessToken.get();
            ensureAthleteFetched(token, syncState);
            syncNewestActivities(token, syncState);
            continueActivityBackfill(token, syncState);
            enrichKudosAndComments(token, syncState);
            continueReconciliationSweep(token, syncState);
            syncState.setLastRunOutcome("COMPLETED");
            syncState.setLastError(null);
        } catch (StravaRateLimitException e) {
            log.info("Sync run stopped by a 429 from Strava; resuming next scheduled run");
            syncState.setLastRunOutcome("RATE_LIMITED");
            syncState.setLastError(e.getMessage());
        } catch (Exception e) {
            log.error("Sync run failed", e);
            syncState.setLastRunOutcome("ERROR");
            syncState.setLastError(e.getMessage());
        } finally {
            syncState.setLastRunFinishedAt(Instant.now());
            syncStateRepository.save(syncState);
        }
    }

    private void ensureAthleteFetched(String token, SyncState syncState) {
        if (rateLimiter.isExhausted()) {
            return;
        }
        Instant lastSynced = syncState.getLastAthleteSyncAt();
        if (lastSynced != null && lastSynced.isAfter(Instant.now().minus(ATHLETE_SYNC_INTERVAL))) {
            return;
        }
        fetchService.fetchAthlete(token);
        syncState.setLastAthleteSyncAt(Instant.now());
    }

    private void syncNewestActivities(String token, SyncState syncState) {
        if (rateLimiter.isExhausted()) {
            return;
        }
        // Anchor to the start of this fetch, not its completion, so anything
        // created while the fetch is in flight is still covered by next
        // tick's window (plus FetchService's own overlap margin on top).
        Instant fetchStartedAt = Instant.now();
        FetchService.FetchResult result = fetchService.fetchRecentActivities(token, syncState.getLastActivitySyncAt());
        if (result.reachedEnd()) {
            syncState.setLastActivitySyncAt(fetchStartedAt);
        }
    }

    private void continueActivityBackfill(String token, SyncState syncState) {
        if (syncState.isActivityBackfillComplete() || rateLimiter.isExhausted()) {
            return;
        }
        FetchService.FetchResult result = fetchService.fetchOlderActivities(token);
        if (result.reachedEnd() && result.fetchedCount() == 0) {
            log.info("Activity backfill complete: reached the start of the account's history");
            syncState.setActivityBackfillComplete(true);
        }
    }

    private void enrichKudosAndComments(String token, SyncState syncState) {
        if (rateLimiter.isExhausted()) {
            return;
        }
        Instant lastSynced = syncState.getLastKudosCommentsSyncAt();
        if (lastSynced != null && lastSynced.isAfter(Instant.now().minus(KUDOS_COMMENTS_SYNC_INTERVAL))) {
            return;
        }
        fetchService.fetchKudos(token);
        if (!rateLimiter.isExhausted()) {
            fetchService.fetchComments(token);
        }
        syncState.setLastKudosCommentsSyncAt(Instant.now());
    }

    /**
     * Fifth, lowest-priority phase: keeps activity names/gear fresh beyond the
     * regular sync's window, and detects activities deleted on Strava. Runs
     * last so it only ever spends budget the phases above didn't need — see
     * docs/reconciliation.md. A sweep may span many ticks; progress resumes
     * from {@code SyncState.reconciliationCursorBefore}.
     */
    private void continueReconciliationSweep(String token, SyncState syncState) {
        if (rateLimiter.isExhausted()) {
            return;
        }

        if (!syncState.isActivityBackfillComplete()) {
            return;
        }

        if (syncState.getReconciliationSweepStartedAt() == null) {
            if (!reconciliationDue(syncState)) {
                return;
            }
            log.info("Starting reconciliation sweep");
            syncState.setReconciliationSweepStartedAt(Instant.now());
            syncState.setReconciliationCursorBefore(null);
        }

        Instant cursor = syncState.getReconciliationCursorBefore() != null
                ? syncState.getReconciliationCursorBefore()
                : syncState.getReconciliationSweepStartedAt();

        FetchService.FetchResult result = fetchService.fetchActivitiesForReconciliation(token, cursor);

        if (result.reachedEnd()) {
            int deleted = fetchService.deleteStaleActivities(syncState.getReconciliationSweepStartedAt());
            log.info("Reconciliation sweep complete; deleted {} activities no longer on Strava", deleted);
            syncState.setReconciliationLastCompletedAt(Instant.now());
            syncState.setReconciliationSweepStartedAt(null);
            syncState.setReconciliationCursorBefore(null);
        } else if (result.oldestFetchedAt() != null) {
            syncState.setReconciliationCursorBefore(result.oldestFetchedAt());
        }
    }

    private boolean reconciliationDue(SyncState syncState) {
        Instant lastCompleted = syncState.getReconciliationLastCompletedAt();
        return lastCompleted == null || lastCompleted.isBefore(Instant.now().minus(RECONCILIATION_INTERVAL));
    }

    private Optional<String> getValidAccessToken() {
        Optional<OAuthToken> token = stravaOAuthService.readOAuthToken();
        if (token.isEmpty()) {
            return Optional.empty();
        }
        if (!stravaOAuthService.isTokenCurrent()) {
            token = stravaOAuthService.refreshToken(token.get().getRefreshToken());
        }
        return token.map(OAuthToken::getAccessToken);
    }
}
