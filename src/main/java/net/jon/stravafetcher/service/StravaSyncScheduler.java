package net.jon.stravafetcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fallback trigger only. Primary sync triggering now happens per-request via
 * {@link SyncTriggerService} — this tick
 * exists so backfill/reconciliation/kudos-comments progress (and a missed
 * request-driven activity sync) still happens during stretches where nobody
 * opens the app.
 */
@Component
public class StravaSyncScheduler {
    private static final Logger log = LoggerFactory.getLogger(StravaSyncScheduler.class);
    private static final long FALLBACK_INTERVAL_MS = 3 * 60 * 60 * 1000;

    private final SyncTriggerService syncTriggerService;

    public StravaSyncScheduler(SyncTriggerService syncTriggerService) {
        this.syncTriggerService = syncTriggerService;
    }

    @Scheduled(fixedDelay = FALLBACK_INTERVAL_MS)
    public void run() {
        log.debug("Starting fallback Strava sync trigger");
        syncTriggerService.trigger();
    }
}
