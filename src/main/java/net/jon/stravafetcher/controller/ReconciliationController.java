package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.model.SyncState;
import net.jon.stravafetcher.repository.SyncStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Manual trigger for the reconciliation sweep (see docs/reconciliation.md),
 * so it can be verified right after deploy instead of waiting up to a month
 * for the next natural trigger. Deliberately unauthenticated — single-user
 * hobby project, and the worst case of misuse is an extra unnecessary sweep.
 */
@RestController
@RequestMapping("/admin/reconciliation")
public class ReconciliationController {
    private static final Logger log = LoggerFactory.getLogger(ReconciliationController.class);

    private final SyncStateRepository syncStateRepository;

    public ReconciliationController(SyncStateRepository syncStateRepository) {
        this.syncStateRepository = syncStateRepository;
    }

    @PostMapping("/trigger")
    public String trigger() {
        SyncState syncState = syncStateRepository.findAll().stream().findFirst().orElseGet(SyncState::new);

        if (syncState.getReconciliationSweepStartedAt() != null) {
            log.info("Reconciliation trigger requested, but a sweep is already in progress");
            return "Reconciliation sweep already in progress";
        }

        syncState.setReconciliationSweepStartedAt(Instant.now());
        syncState.setReconciliationCursorBefore(null);
        syncStateRepository.save(syncState);

        log.info("Reconciliation sweep triggered manually");
        if (!syncState.isActivityBackfillComplete()) {
            return "Reconciliation sweep queued; it's gated behind activity backfill, which hasn't finished yet, so it will start once that completes.";
        }
        return "Reconciliation sweep started; the next scheduled sync run will begin working through it.";
    }
}
