package net.jon.stravafetcher.service;

import net.jon.stravafetcher.model.*;
import net.jon.stravafetcher.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class FetchService {
    public static final int PER_PAGE = 100;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private FollowerRepository followerRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private RideActivityRepository rideActivityRepository;
    @Autowired
    private KudosRepository kudosRepository;
    @Autowired
    private StravaService stravaService;

    private static final Logger log = LoggerFactory.getLogger(FetchService.class);

    public void fetchRecent(String accessToken, int months) {
        ZonedDateTime before = ZonedDateTime.now();
        ZonedDateTime after = before.minus(months, ChronoUnit.MONTHS);
        fetchAll(accessToken, after, before);
    }

    public void fetchHistory(String accessToken, int months) {
        RideActivity rideActivity = rideActivityRepository.findOldestRideActivity();
        ZoneOffset zoneOffset = ZoneOffset.of(rideActivity.getTimezone());
        ZonedDateTime before = rideActivity.getStartDateLocal().atOffset(zoneOffset).toZonedDateTime();
        ZonedDateTime after = before.minus(months, ChronoUnit.MONTHS);
        ZonedDateTime januaryFirst2010 = ZonedDateTime.of(2010, 1, 1, 0, 0, 0, 0, zoneOffset);
        if (after.isAfter(januaryFirst2010)) {
            fetchAll(accessToken, after, before);
        } else {
            log.info("No more activities to fetch.");
        }
    }

    private void fetchAll(String accessToken, ZonedDateTime after, ZonedDateTime before) {
        log.debug("Fetching all activities between {} and {}", after, before);
        fetchAthlete(accessToken);
        fetchActivities(accessToken, after, before);
        fetchKudos(accessToken, after.toLocalDateTime(), before.toLocalDateTime());
        fetchComments(accessToken, after.toLocalDateTime(), before.toLocalDateTime());
    }

    private void fetchAthlete(String accessToken) {
        Athlete athlete = stravaService.getAthlete(accessToken);
        log.debug("Fetched athlete {}", athlete);
        athlete.getBikes().forEach(bike -> {
            bike.setAthlete(athlete);
        });
        athleteRepository.save(athlete);
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

    public void fetchComments(String accessToken, LocalDateTime after, LocalDateTime before) {
        rideActivityRepository.findActivitiesBetween(after, before).forEach(activity -> {
            log.debug("Fetching comments for activity {}", activity);
            stravaService.getActivityComments(accessToken, activity.getId()).forEach(comment -> {
                Follower existingFollower = followerRepository.findByFirstNameAndLastName(comment.getFollower().getFirstName(), comment.getFollower().getLastName());
                comment.setFollower(Objects.requireNonNullElseGet(existingFollower, () -> followerRepository.save(comment.getFollower())));
                commentRepository.save(comment);
            });
        });
    }

    public void fetchKudos(String accessToken,LocalDateTime after, LocalDateTime before) {
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
}
