package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.AthleteRepository;
import net.jon.stravafetcher.repository.BikeRepository;
import net.jon.stravafetcher.repository.RideActivityRepository;
import net.jon.stravafetcher.service.StravaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/fetch")
public class FetchController {
    public static final int PER_PAGE = 100;
    public static final int AFTER_YEAR = 2022;
    private static final Logger log = LoggerFactory.getLogger(FetchController.class);
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private RideActivityRepository rideActivityRepository;
    @Autowired
    private StravaService stravaService;

    @GetMapping("/activities/{accessToken}")
    public int fetchActivities(@PathVariable String accessToken) {
        getAthlete(accessToken);
        return getActivities(accessToken);
    }

    private Athlete getAthlete(String accessToken) {
        Athlete athlete = stravaService.getAthlete(accessToken);
        log.debug("Fetched athlete {}", athlete);

        athlete.getBikes().forEach(bike -> {
            bike.setAthlete(athlete);
        });

        if (!athleteRepository.existsById(athlete.getId())) {
            athleteRepository.save(athlete);
        }

        return athlete;
    }

    private int getActivities(String accessToken) {
        ZonedDateTime afterDateTime = getAfterDateTime();
        log.info("Fetching activities after {}", afterDateTime);

        int page = 1;
        int fetched = 0;

        while (true) {
            List<RideActivity> activities = stravaService.getActivities(
                    accessToken,
                    page,
                    PER_PAGE,
                    afterDateTime.toEpochSecond());

            if (activities.isEmpty()) {
                break;
            }

            activities.forEach(rideActivityRepository::save);
            page++;
            fetched += activities.size();
            log.debug("Fetched {} activities", fetched);
        }

        log.debug("Fetched a total of {} activities", fetched);
        return fetched;
    }

    private ZonedDateTime getAfterDateTime() {
        RideActivity rideActivity = rideActivityRepository.findMostRecentRideActivity();

        if (rideActivity == null) {
            LocalDateTime localDateTime = LocalDateTime.of(AFTER_YEAR, 1, 1, 0, 0);
            ZoneId zoneId = ZoneId.systemDefault();
            return ZonedDateTime.of(localDateTime, zoneId);
        }

        ZoneId zoneId = ZoneId.of(rideActivity.getTimezone());
        return rideActivity.getStartDateLocal().atZone(zoneId);
    }
}
