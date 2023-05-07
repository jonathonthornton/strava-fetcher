package net.jon.stravafetcher.service;

import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.CommentAuthor;
import net.jon.stravafetcher.model.CommentAuthorRepository;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.AthleteRepository;
import net.jon.stravafetcher.repository.CommentRepository;
import net.jon.stravafetcher.repository.RideActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private CommentAuthorRepository commentAuthorRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private RideActivityRepository rideActivityRepository;
    @Autowired
    private StravaService stravaService;

    private static final Logger log = LoggerFactory.getLogger(FetchService.class);

    public void fetchRecent(String accessToken) {
        ZonedDateTime before = ZonedDateTime.now();
        ZonedDateTime after = before.minus(2, ChronoUnit.WEEKS);
        fetchAll(accessToken, after, before);
    }

    public void fetchHistory(String accessToken) {
        RideActivity rideActivity = rideActivityRepository.findOldestRideActivity();
        ZoneOffset zoneOffset = ZoneOffset.of(rideActivity.getTimezone());
        ZonedDateTime before = rideActivity.getStartDateLocal().atOffset(zoneOffset).toZonedDateTime();
        ZonedDateTime after = before.minus(2, ChronoUnit.WEEKS);
        fetchAll(accessToken, after, before);
    }

    private void fetchAll(String accessToken, ZonedDateTime after, ZonedDateTime before) {
        log.debug("Fetching all activities between {} and {}", after, before);
        fetchAthlete(accessToken);
        fetchActivities(accessToken, after, before);
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
        log.info("Fetching activities between {} and {}", after, before);
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
                CommentAuthor existingAuthor = commentAuthorRepository.findByFirstNameAndLastName(comment.getCommentAuthor().getFirstName(), comment.getCommentAuthor().getLastName());
                comment.setCommentAuthor(Objects.requireNonNullElseGet(existingAuthor, () -> commentAuthorRepository.save(comment.getCommentAuthor())));
                commentRepository.save(comment);
            });
        });
    }

}
