package net.jon.stravafetcher.service;

import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.Follower;
import net.jon.stravafetcher.model.Kudos;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class FetchService {
    private static final int PER_PAGE = 100;
    private final CommentRepository commentRepository;
    private final FollowerRepository followerRepository;
    private final AthleteRepository athleteRepository;
    private final RideActivityRepository rideActivityRepository;
    private final KudosRepository kudosRepository;
    private final StravaService stravaService;
    private static final Logger log = LoggerFactory.getLogger(FetchService.class);

    public FetchService(
            CommentRepository commentRepository,
            FollowerRepository followerRepository,
            AthleteRepository athleteRepository,
            RideActivityRepository rideActivityRepository,
            KudosRepository kudosRepository,
            StravaService stravaService) {
        this.commentRepository = commentRepository;
        this.followerRepository = followerRepository;
        this.athleteRepository = athleteRepository;
        this.rideActivityRepository = rideActivityRepository;
        this.kudosRepository = kudosRepository;
        this.stravaService = stravaService;
    }

    public void fetchAthlete(String accessToken) {
        Athlete athlete = stravaService.getAthlete(accessToken);
        log.debug("Fetched athlete {}", athlete);
        athlete.getBikes().forEach(bike -> {
            bike.setAthlete(athlete);
        });
        athleteRepository.save(athlete);
    }

    public void fetchActivities(String accessToken) {
        rideActivityRepository.findOldestRideActivity()
                .ifPresentOrElse(
                        oldestActivity -> {
                            ZonedDateTime before = oldestActivity.getStartDateLocal().atZone(ZoneOffset.of(oldestActivity.getTimezone()));
                            ZonedDateTime after = before.minus(6, ChronoUnit.MONTHS);
                            log.info("Fetching activities between {} and {}", after, before);
                            fetchActivities(accessToken, after, before);
                        },
                        () -> {
                            ZonedDateTime before = ZonedDateTime.now();
                            ZonedDateTime after = before.minus(1, ChronoUnit.MONTHS);
                            log.info("Fetching activities between {} and {}", after, before);
                            fetchActivities(accessToken, after, before);
                        });
    }

    public void fetchKudos(String accessToken) {
        log.debug("Fetching kudos");
        rideActivityRepository.findPublicActivitiesWithMismatchedKudosCounts().forEach(activity -> {
            log.debug("Fetching kudos for activity {}", activity);
            stravaService.getActivityKudos(accessToken, activity.getId()).forEach(follower -> {
                Kudos kudos = new Kudos();
                kudos.setActivityId(activity.getId());
                Follower existingFollower = followerRepository.findByFirstNameAndLastName(follower.getFirstName(), follower.getLastName());
                kudos.setFollower(Objects.requireNonNullElseGet(existingFollower, () -> followerRepository.save(follower)));
                kudosRepository.save(kudos);
            });
        });
    }

    public void fetchComments(String accessToken) {
        log.debug("Fetching comments");
        rideActivityRepository.findPublicActivitiesWithMismatchedCommentCounts().forEach(activity -> {
            log.debug("Fetching comments for activity {}", activity);
            stravaService.getActivityComments(accessToken, activity.getId()).forEach(comment -> {
                Follower existingFollower = followerRepository.findByFirstNameAndLastName(comment.getFollower().getFirstName(), comment.getFollower().getLastName());
                comment.setFollower(Objects.requireNonNullElseGet(existingFollower, () -> followerRepository.save(comment.getFollower())));
                commentRepository.save(comment);
            });
        });
    }

    private void fetchActivities(String accessToken, ZonedDateTime after, ZonedDateTime before) {
        int page = 1;
        int fetched = 0;
        int activitiesCount;

        do {
            List<RideActivity> activities = stravaService.getActivities(
                    accessToken,
                    page,
                    PER_PAGE,
                    after.toEpochSecond(),
                    before.toEpochSecond());

            activitiesCount = activities.size();

            if (activitiesCount > 0) {
                activities.forEach(rideActivityRepository::save);
                page++;
                fetched += activitiesCount;
                log.debug("Fetched {} activities", fetched);
            }
        } while (activitiesCount > 0);

        log.debug("Fetched a total of {} activities", fetched);
    }
}
