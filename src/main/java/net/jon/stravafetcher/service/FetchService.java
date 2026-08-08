package net.jon.stravafetcher.service;

import net.jon.stravafetcher.client.StravaRateLimiter;
import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.Follower;
import net.jon.stravafetcher.model.Kudos;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FetchService {
    private static final int PER_PAGE = 100;
    private static final int MONTHS_TO_FETCH = 6;
    private static final Duration SYNC_OVERLAP_MARGIN = Duration.ofMinutes(5);
    private static final ZonedDateTime EARLIEST_POSSIBLE_DATE = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final Logger log = LoggerFactory.getLogger(FetchService.class);
    private final CommentRepository commentRepository;
    private final FollowerRepository followerRepository;
    private final AthleteRepository athleteRepository;
    private final RideActivityRepository rideActivityRepository;
    private final KudosRepository kudosRepository;
    private final StravaService stravaService;
    private final StravaRateLimiter rateLimiter;

    public FetchService(
            CommentRepository commentRepository,
            FollowerRepository followerRepository,
            AthleteRepository athleteRepository,
            RideActivityRepository rideActivityRepository,
            KudosRepository kudosRepository,
            StravaService stravaService,
            StravaRateLimiter rateLimiter) {
        this.commentRepository = commentRepository;
        this.followerRepository = followerRepository;
        this.athleteRepository = athleteRepository;
        this.rideActivityRepository = rideActivityRepository;
        this.kudosRepository = kudosRepository;
        this.stravaService = stravaService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Result of a windowed activity fetch. {@code reachedEnd} means pagination
     * within the requested window ran to completion (Strava returned a
     * short page); {@code stoppedOnBudget} means it was cut short because the
     * rate-limit budget ran low and should be resumed on a later run.
     * {@code oldestFetchedAt} is the start time of the oldest activity seen
     * in this call (null if nothing was fetched), used by the reconciliation
     * sweep to advance its resume cursor.
     */
    public record FetchResult(int fetchedCount, boolean stoppedOnBudget, boolean reachedEnd, Instant oldestFetchedAt) {
    }

    public void fetchAthlete(String accessToken) {
        Athlete athlete = stravaService.getAthlete(accessToken);
        log.debug("Fetched athlete {}", athlete);
        athlete.getBikes().forEach(bike -> {
            bike.setAthlete(athlete);
        });
        athleteRepository.save(athlete);
    }

    public FetchResult fetchAllActivities(String accessToken) {
        log.debug("Fetching all activities");
        ZonedDateTime before = ZonedDateTime.now();
        log.info("Fetching activities between {} and {}", EARLIEST_POSSIBLE_DATE, before);
        return fetchActivities(accessToken, EARLIEST_POSSIBLE_DATE, before);
    }

    /**
     * Continues the reconciliation sweep: fetches one batch of activities
     * walking backward from {@code before}, with no lower bound, re-saving
     * (and re-stamping {@code lastSyncedAt} on) everything returned. Unlike
     * {@link #fetchOlderActivities}, the cursor is caller-supplied rather
     * than derived from the DB's current oldest row, since the sweep needs
     * to revisit the entire history independent of what backfill has
     * already covered.
     */
    public FetchResult fetchActivitiesForReconciliation(String accessToken, Instant before) {
        log.debug("Fetching activities for reconciliation sweep");
        ZonedDateTime beforeZdt = before.atZone(ZoneOffset.UTC);
        log.info("Reconciliation: fetching activities before {}", beforeZdt);
        return fetchActivities(accessToken, EARLIEST_POSSIBLE_DATE, beforeZdt);
    }

    /**
     * Deletes activities that weren't touched by a save since {@code cutoff}
     * (a completed reconciliation sweep's start time) — i.e. Strava no
     * longer reported them anywhere during a full pass. Also cleans up
     * their kudos/comments, which aren't cascaded automatically since
     * neither entity has a JPA relation back to {@link RideActivity}.
     */
    @Transactional
    public int deleteStaleActivities(Instant cutoff) {
        List<RideActivity> staleActivities = rideActivityRepository.findStaleActivities(cutoff);
        if (staleActivities.isEmpty()) {
            return 0;
        }

        staleActivities.forEach(activity -> log.info(
                "Deleting activity no longer on Strava: id={} name={} date={}",
                activity.getId(), activity.getName(), activity.getStartDateLocal()));

        List<Long> ids = staleActivities.stream().map(RideActivity::getId).toList();
        kudosRepository.deleteByActivityIdIn(ids);
        commentRepository.deleteByActivityIdIn(ids);
        rideActivityRepository.deleteAll(staleActivities);

        return staleActivities.size();
    }

    /**
     * Fetches activities since {@code lastSyncAt} (a small overlap margin is
     * subtracted to cover clock skew and activities that landed mid-fetch
     * last time), falling back to the old 6-month window when there's no
     * prior sync to anchor to yet (fresh deploy, or upgrading from before
     * this field existed).
     */
    public FetchResult fetchRecentActivities(String accessToken, Instant lastSyncAt) {
        log.debug("Fetching recent activities");
        ZonedDateTime before = ZonedDateTime.now();
        ZonedDateTime after = lastSyncAt != null
                ? lastSyncAt.minus(SYNC_OVERLAP_MARGIN).atZone(ZoneOffset.UTC)
                : before.minus(MONTHS_TO_FETCH, ChronoUnit.MONTHS);
        log.info("Fetching activities between {} and {}", after, before);
        return fetchActivities(accessToken, after, before);
    }

    public FetchResult fetchOlderActivities(String accessToken) {
        Optional<RideActivity> oldestActivity = rideActivityRepository.findOldestRideActivity();
        ZonedDateTime before = oldestActivity
                .map(activity -> activity.getStartDateLocal().atZone(ZoneOffset.of(activity.getTimezone())))
                .orElseGet(ZonedDateTime::now);
        ZonedDateTime after = before.minus(MONTHS_TO_FETCH, ChronoUnit.MONTHS);
        log.info("Fetching activities between {} and {}", after, before);
        return fetchActivities(accessToken, after, before);
    }

    public void fetchKudos(String accessToken) {
        log.debug("Fetching kudos");
        for (RideActivity activity : rideActivityRepository.findPublicActivitiesWithMismatchedKudosCounts()) {
            if (!rateLimiter.hasBudgetFor(1)) {
                log.info("Rate limit budget low, pausing kudos fetch");
                break;
            }
            log.debug("Fetching kudos for activity {}", activity);
            stravaService.getActivityKudos(accessToken, activity.getId()).forEach(follower -> {
                Kudos kudos = new Kudos();
                kudos.setActivityId(activity.getId());
                Follower existingFollower = followerRepository.findByFirstNameAndLastName(follower.getFirstName(), follower.getLastName());
                kudos.setFollower(Objects.requireNonNullElseGet(existingFollower, () -> followerRepository.save(follower)));
                kudosRepository.save(kudos);
            });
        }
    }

    public void fetchComments(String accessToken) {
        log.debug("Fetching comments");
        for (RideActivity activity : rideActivityRepository.findPublicActivitiesWithMismatchedCommentCounts()) {
            if (!rateLimiter.hasBudgetFor(1)) {
                log.info("Rate limit budget low, pausing comments fetch");
                break;
            }
            log.debug("Fetching comments for activity {}", activity);
            stravaService.getActivityComments(accessToken, activity.getId()).forEach(comment -> {
                Follower existingFollower = followerRepository.findByFirstNameAndLastName(comment.getFollower().getFirstName(), comment.getFollower().getLastName());
                comment.setFollower(Objects.requireNonNullElseGet(existingFollower, () -> followerRepository.save(comment.getFollower())));
                commentRepository.save(comment);
            });
        }
    }

    private FetchResult fetchActivities(String accessToken, ZonedDateTime after, ZonedDateTime before) {
        int page = 1;
        int fetched = 0;
        int activitiesCount;
        Instant oldestFetchedAt = null;

        do {
            if (!rateLimiter.hasBudgetFor(1)) {
                log.info("Rate limit budget low, pausing activity fetch at page {}", page);
                return new FetchResult(fetched, true, false, oldestFetchedAt);
            }

            List<RideActivity> activities = stravaService.getActivities(
                    accessToken,
                    page,
                    PER_PAGE,
                    after.toEpochSecond(),
                    before.toEpochSecond());

            activitiesCount = activities.size();

            if (activitiesCount > 0) {
                Instant fetchedAt = Instant.now();
                activities.forEach(activity -> {
                    // Set explicitly (rather than via a @PrePersist/@PreUpdate callback) so it's
                    // always dirty and triggers a real UPDATE even when re-saving an activity whose
                    // other fields are unchanged from what's already in the DB — the reconciliation
                    // sweep's deletion pass relies on this timestamp actually advancing on every save.
                    activity.setLastSyncedAt(fetchedAt);
                    rideActivityRepository.save(activity);
                });
                // Strava returns activities newest-first within a page, so the last one is the oldest.
                RideActivity oldestInPage = activities.get(activities.size() - 1);
                oldestFetchedAt = oldestInPage.getStartDateLocal()
                        .atZone(ZoneOffset.of(oldestInPage.getTimezone()))
                        .toInstant();
                page++;
                fetched += activitiesCount;
                log.debug("Fetched {} activities", fetched);
            }
        } while (activitiesCount == PER_PAGE);

        log.debug("Fetched a total of {} activities", fetched);
        return new FetchResult(fetched, false, true, oldestFetchedAt);
    }
}
