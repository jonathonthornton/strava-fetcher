package net.jon.stravafetcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Single entry point for kicking off {@link StravaSyncOrchestrator#runBudgetedSync()},
 * called both by incoming requests (via the sync-trigger interceptor) and by the
 * fallback {@link StravaSyncScheduler} tick. Every run goes through the same
 * single-thread executor, so the two trigger sources can never race each other
 * on {@code SyncState}. A cooldown and an in-flight guard keep a burst of
 * requests (e.g. an SPA page load firing several API calls at once) from
 * queuing up redundant runs.
 */
@Service
public class SyncTriggerService {
    private static final Logger log = LoggerFactory.getLogger(SyncTriggerService.class);
    private static final Duration COOLDOWN = Duration.ofMinutes(5);

    private final StravaSyncOrchestrator orchestrator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "strava-sync-trigger");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean triggering = new AtomicBoolean(false);
    private final AtomicReference<Instant> lastTriggeredAt = new AtomicReference<>();

    public SyncTriggerService(StravaSyncOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void trigger() {
        Instant now = Instant.now();
        Instant last = lastTriggeredAt.get();
        if (last != null && last.isAfter(now.minus(COOLDOWN))) {
            return;
        }
        if (!triggering.compareAndSet(false, true)) {
            return;
        }
        lastTriggeredAt.set(now);
        executor.submit(() -> {
            try {
                orchestrator.runBudgetedSync();
            } catch (Exception e) {
                log.error("Triggered sync run failed", e);
            } finally {
                triggering.set(false);
            }
        });
    }
}
