package net.jon.stravafetcher.service;

import net.jon.stravafetcher.client.StravaRateLimiter;
import net.jon.stravafetcher.exception.StravaRateLimitException;
import net.jon.stravafetcher.model.OAuthToken;
import net.jon.stravafetcher.model.SyncState;
import net.jon.stravafetcher.repository.SyncStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Runs one budgeted pass of the Strava sync: refresh the newest activities,
 * continue the backward backfill if it isn't finished, then spend whatever
 * rate-limit budget is left on kudos/comments enrichment. Each phase checks
 * the shared {@link StravaRateLimiter} before doing any work, so a single
 * run stops cleanly instead of tripping a 429; the next scheduled run picks
 * up wherever this one left off.
 */
@Service
public class StravaSyncOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(StravaSyncOrchestrator.class);

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
            ensureAthleteFetched(token);
            syncNewestActivities(token);
            continueActivityBackfill(token, syncState);
            enrichKudosAndComments(token);
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

    private void ensureAthleteFetched(String token) {
        if (rateLimiter.isExhausted()) {
            return;
        }
        fetchService.fetchAthlete(token);
    }

    private void syncNewestActivities(String token) {
        if (rateLimiter.isExhausted()) {
            return;
        }
        fetchService.fetchRecentActivities(token);
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

    private void enrichKudosAndComments(String token) {
        if (rateLimiter.isExhausted()) {
            return;
        }
        fetchService.fetchKudos(token);
        if (!rateLimiter.isExhausted()) {
            fetchService.fetchComments(token);
        }
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
