package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.client.StravaRateLimiter;
import net.jon.stravafetcher.dto.SyncStatusDTO;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.RideActivityRepository;
import net.jon.stravafetcher.repository.SyncStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
public class SyncStatusController {
    private static final Logger log = LoggerFactory.getLogger(SyncStatusController.class);

    private final RideActivityRepository rideActivityRepository;
    private final SyncStateRepository syncStateRepository;
    private final StravaRateLimiter rateLimiter;

    public SyncStatusController(
            RideActivityRepository rideActivityRepository,
            SyncStateRepository syncStateRepository,
            StravaRateLimiter rateLimiter) {
        this.rideActivityRepository = rideActivityRepository;
        this.syncStateRepository = syncStateRepository;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/status")
    public SyncStatusDTO status() {
        log.debug("status called");
        SyncStatusDTO dto = new SyncStatusDTO();
        dto.setTotalActivities(rideActivityRepository.count());

        RideActivity newest = rideActivityRepository.findNewestRideActivity();
        if (newest != null) {
            dto.setNewestActivityDate(newest.getStartDateLocal());
        }
        rideActivityRepository.findOldestRideActivity()
                .ifPresent(oldest -> dto.setOldestActivityDate(oldest.getStartDateLocal()));

        dto.setPendingKudosCount(rideActivityRepository.findPublicActivitiesWithMismatchedKudosCounts().size());
        dto.setPendingCommentsCount(rideActivityRepository.findPublicActivitiesWithMismatchedCommentCounts().size());

        syncStateRepository.findAll().stream().findFirst().ifPresent(syncState -> {
            dto.setActivityBackfillComplete(syncState.isActivityBackfillComplete());
            dto.setLastRunAt(syncState.getLastRunFinishedAt());
            dto.setLastRunOutcome(syncState.getLastRunOutcome());
            dto.setReconciliationInProgress(syncState.getReconciliationSweepStartedAt() != null);
            dto.setReconciliationLastCompletedAt(syncState.getReconciliationLastCompletedAt());
        });

        dto.setShortTermUsage(rateLimiter.getShortTermUsage());
        dto.setShortTermLimit(rateLimiter.getShortTermLimit());
        dto.setDailyUsage(rateLimiter.getDailyUsage());
        dto.setDailyLimit(rateLimiter.getDailyLimit());

        return dto;
    }
}
