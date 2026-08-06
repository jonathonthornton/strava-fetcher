package net.jon.stravafetcher.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class SyncStatusDTO {
    private long totalActivities;
    private LocalDateTime oldestActivityDate;
    private LocalDateTime newestActivityDate;
    private boolean activityBackfillComplete;
    private long pendingKudosCount;
    private long pendingCommentsCount;
    private int shortTermUsage;
    private int shortTermLimit;
    private int dailyUsage;
    private int dailyLimit;
    private Instant lastRunAt;
    private String lastRunOutcome;

    public long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public LocalDateTime getOldestActivityDate() {
        return oldestActivityDate;
    }

    public void setOldestActivityDate(LocalDateTime oldestActivityDate) {
        this.oldestActivityDate = oldestActivityDate;
    }

    public LocalDateTime getNewestActivityDate() {
        return newestActivityDate;
    }

    public void setNewestActivityDate(LocalDateTime newestActivityDate) {
        this.newestActivityDate = newestActivityDate;
    }

    public boolean isActivityBackfillComplete() {
        return activityBackfillComplete;
    }

    public void setActivityBackfillComplete(boolean activityBackfillComplete) {
        this.activityBackfillComplete = activityBackfillComplete;
    }

    public long getPendingKudosCount() {
        return pendingKudosCount;
    }

    public void setPendingKudosCount(long pendingKudosCount) {
        this.pendingKudosCount = pendingKudosCount;
    }

    public long getPendingCommentsCount() {
        return pendingCommentsCount;
    }

    public void setPendingCommentsCount(long pendingCommentsCount) {
        this.pendingCommentsCount = pendingCommentsCount;
    }

    public int getShortTermUsage() {
        return shortTermUsage;
    }

    public void setShortTermUsage(int shortTermUsage) {
        this.shortTermUsage = shortTermUsage;
    }

    public int getShortTermLimit() {
        return shortTermLimit;
    }

    public void setShortTermLimit(int shortTermLimit) {
        this.shortTermLimit = shortTermLimit;
    }

    public int getDailyUsage() {
        return dailyUsage;
    }

    public void setDailyUsage(int dailyUsage) {
        this.dailyUsage = dailyUsage;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public String getLastRunOutcome() {
        return lastRunOutcome;
    }

    public void setLastRunOutcome(String lastRunOutcome) {
        this.lastRunOutcome = lastRunOutcome;
    }
}
