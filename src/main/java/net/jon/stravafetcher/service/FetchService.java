package net.jon.stravafetcher.service;

import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.Follower;
import net.jon.stravafetcher.model.Kudos;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static final String MELBOURNE_TIMEZONE = "Australia/Melbourne";

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

    public void fetchAll(String accessToken, int afterYear, int afterMonth, int beforeYear, int beforeMonth) {
        fetchAthlete(accessToken);
        fetchActivities(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
        fetchKudos(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
        fetchComments(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
    }

    public void fetchActivities(String accessToken, int afterYear, int afterMonth, int beforeYear, int beforeMonth) {
        ZonedDateTime after = ZonedDateTime.of(afterYear, afterMonth, 1, 0, 0, 0, 0, ZoneId.of(MELBOURNE_TIMEZONE));
        ZonedDateTime before = ZonedDateTime.of(beforeYear, beforeMonth, 1, 0, 0, 0, 0, ZoneId.of(MELBOURNE_TIMEZONE));
        fetchActivities(accessToken, after, before);
    }

    public void fetchKudos(String accessToken, int afterYear, int afterMonth, int beforeYear, int beforeMonth) {
        ZonedDateTime after = ZonedDateTime.of(afterYear, afterMonth, 1, 0, 0, 0, 0, ZoneId.of(MELBOURNE_TIMEZONE));
        ZonedDateTime before = ZonedDateTime.of(beforeYear, beforeMonth, 1, 0, 0, 0, 0, ZoneId.of(MELBOURNE_TIMEZONE));
        fetchKudos(accessToken, after.toLocalDateTime(), before.toLocalDateTime());
    }

    public void fetchComments(String accessToken, int afterYear, int afterMonth, int beforeYear, int beforeMonth) {
        ZonedDateTime after = ZonedDateTime.of(afterYear, afterMonth, 1, 0, 0, 0, 0, ZoneId.of(MELBOURNE_TIMEZONE));
        ZonedDateTime before = ZonedDateTime.of(beforeYear, beforeMonth, 1, 0, 0, 0, 0, ZoneId.of(MELBOURNE_TIMEZONE));
        fetchComments(accessToken, after.toLocalDateTime(), before.toLocalDateTime());
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

    private void fetchKudos(String accessToken,LocalDateTime after, LocalDateTime before) {
        rideActivityRepository.findActivitiesBetween(after, before).forEach(activity -> {
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

    private void fetchComments(String accessToken, LocalDateTime after, LocalDateTime before) {
        rideActivityRepository.findActivitiesBetween(after, before).forEach(activity -> {
            log.debug("Fetching comments for activity {}", activity);
            stravaService.getActivityComments(accessToken, activity.getId()).forEach(comment -> {
                Follower existingFollower = followerRepository.findByFirstNameAndLastName(comment.getFollower().getFirstName(), comment.getFollower().getLastName());
                comment.setFollower(Objects.requireNonNullElseGet(existingFollower, () -> followerRepository.save(comment.getFollower())));
                commentRepository.save(comment);
            });
        });
    }
}
