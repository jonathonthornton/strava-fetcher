package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.model.ActivityMap;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.RideActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/map")
public class ActivityMapController {
    private static final Logger log = LoggerFactory.getLogger(ActivityMapController.class);

    private final RideActivityRepository rideActivityRepository;

    public ActivityMapController(RideActivityRepository rideActivityRepository) {
        this.rideActivityRepository = rideActivityRepository;
    }

    @GetMapping("/{id}")
    public ActivityMap getMap(@PathVariable long id) {
        log.debug("getMap called with id={}", id);
        ActivityMap map = rideActivityRepository.findById(id)
                .map(RideActivity::getMap)
                .orElse(null);
        if (map == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No map found for activity " + id);
        }
        return map;
    }
}
