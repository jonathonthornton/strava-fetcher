package net.jon.stravafetcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StravaSyncScheduler {
    private static final Logger log = LoggerFactory.getLogger(StravaSyncScheduler.class);
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000;

    private final StravaSyncOrchestrator orchestrator;

    public StravaSyncScheduler(StravaSyncOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(fixedDelay = TEN_MINUTES_MS)
    public void run() {
        log.debug("Starting scheduled Strava sync run");
        orchestrator.runBudgetedSync();
    }
}
