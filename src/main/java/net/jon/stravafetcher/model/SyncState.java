package net.jon.stravafetcher.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Single-row table recording backfill progress that can't be derived
 * from ride_activity itself (e.g. whether we've walked all the way back
 * to the start of the account's history).
 */
@Entity
@Table(name = "sync_state")
public class SyncState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "activity_backfill_complete")
    private boolean activityBackfillComplete;

    @Column(name = "last_run_started_at")
    private Instant lastRunStartedAt;

    @Column(name = "last_run_finished_at")
    private Instant lastRunFinishedAt;

    @Column(name = "last_run_outcome")
    private String lastRunOutcome;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "reconciliation_sweep_started_at")
    private Instant reconciliationSweepStartedAt;

    @Column(name = "reconciliation_cursor_before")
    private Instant reconciliationCursorBefore;

    @Column(name = "reconciliation_last_completed_at")
    private Instant reconciliationLastCompletedAt;

    @Column(name = "last_activity_sync_at")
    private Instant lastActivitySyncAt;

    @Column(name = "last_athlete_sync_at")
    private Instant lastAthleteSyncAt;

    @Column(name = "last_kudos_comments_sync_at")
    private Instant lastKudosCommentsSyncAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isActivityBackfillComplete() {
        return activityBackfillComplete;
    }

    public void setActivityBackfillComplete(boolean activityBackfillComplete) {
        this.activityBackfillComplete = activityBackfillComplete;
    }

    public Instant getLastRunStartedAt() {
        return lastRunStartedAt;
    }

    public void setLastRunStartedAt(Instant lastRunStartedAt) {
        this.lastRunStartedAt = lastRunStartedAt;
    }

    public Instant getLastRunFinishedAt() {
        return lastRunFinishedAt;
    }

    public void setLastRunFinishedAt(Instant lastRunFinishedAt) {
        this.lastRunFinishedAt = lastRunFinishedAt;
    }

    public String getLastRunOutcome() {
        return lastRunOutcome;
    }

    public void setLastRunOutcome(String lastRunOutcome) {
        this.lastRunOutcome = lastRunOutcome;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getReconciliationSweepStartedAt() {
        return reconciliationSweepStartedAt;
    }

    public void setReconciliationSweepStartedAt(Instant reconciliationSweepStartedAt) {
        this.reconciliationSweepStartedAt = reconciliationSweepStartedAt;
    }

    public Instant getReconciliationCursorBefore() {
        return reconciliationCursorBefore;
    }

    public void setReconciliationCursorBefore(Instant reconciliationCursorBefore) {
        this.reconciliationCursorBefore = reconciliationCursorBefore;
    }

    public Instant getReconciliationLastCompletedAt() {
        return reconciliationLastCompletedAt;
    }

    public void setReconciliationLastCompletedAt(Instant reconciliationLastCompletedAt) {
        this.reconciliationLastCompletedAt = reconciliationLastCompletedAt;
    }

    public Instant getLastActivitySyncAt() {
        return lastActivitySyncAt;
    }

    public void setLastActivitySyncAt(Instant lastActivitySyncAt) {
        this.lastActivitySyncAt = lastActivitySyncAt;
    }

    public Instant getLastAthleteSyncAt() {
        return lastAthleteSyncAt;
    }

    public void setLastAthleteSyncAt(Instant lastAthleteSyncAt) {
        this.lastAthleteSyncAt = lastAthleteSyncAt;
    }

    public Instant getLastKudosCommentsSyncAt() {
        return lastKudosCommentsSyncAt;
    }

    public void setLastKudosCommentsSyncAt(Instant lastKudosCommentsSyncAt) {
        this.lastKudosCommentsSyncAt = lastKudosCommentsSyncAt;
    }
}
