package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.dto.DistanceRangeCountDTO;
import net.jon.stravafetcher.dto.EarliestRideByBikeDTO;
import net.jon.stravafetcher.dto.LongRidesByBikeDTO;
import net.jon.stravafetcher.dto.LongRidesPerYearDTO;
import net.jon.stravafetcher.dto.OdometerDTO;
import net.jon.stravafetcher.dto.RidesByBikeDTO;
import net.jon.stravafetcher.mapper.RideActivityMapper;
import net.jon.stravafetcher.dto.RideActivityDTO;
import net.jon.stravafetcher.repository.BikeRepository;
import net.jon.stravafetcher.repository.RideActivityRepository;
import net.jon.stravafetcher.service.EddingtonNumberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {
    private static final Logger log = LoggerFactory.getLogger(ActivityController.class);
    private static final int LONG_RIDE_DISTANCE = 200;

    private final RideActivityRepository rideActivityRepository;
    private final BikeRepository bikeRepository;
    private final EddingtonNumberService eddingtonNumberService;

    public ActivityController(RideActivityRepository rideActivityRepository, BikeRepository bikeRepository, EddingtonNumberService eddingtonNumberService) {
        this.rideActivityRepository = rideActivityRepository;
        this.bikeRepository = bikeRepository;
        this.eddingtonNumberService = eddingtonNumberService;
    }

    @GetMapping("/count")
    public long getActivityCount() {
        log.debug("getActivityCount called");
        return rideActivityRepository.count();
    }

    @GetMapping("/eddington-number")
    public int getEddingtonNumber() {
        log.debug("getEddingtonNumber called");
        return eddingtonNumberService.calculateEddingtonNumber();
    }

    @GetMapping("/recent/{limit}")
    public List<RideActivityDTO> getRecentRides(@PathVariable int limit) {
        log.debug("getRecentRides called with limit={}", limit);
        return rideActivityRepository.findRecentRides(limit).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/fast-rides")
    public List<RideActivityDTO> getFastRides() {
        log.debug("getFastRides called");
        return rideActivityRepository.findFastestRides(100).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/long-rides")
    public List<RideActivityDTO> getLongRides() {
        log.debug("getLongRides called");
        return rideActivityRepository.findRidesLongerThan(LONG_RIDE_DISTANCE).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/long-rides-by-bike")
    public List<LongRidesByBikeDTO> getLongRidesByBike() {
        log.debug("getLongRidesByBike called");
        return rideActivityRepository.findLongRidesByBike(LONG_RIDE_DISTANCE);
    }

    @GetMapping("/long-rides-per-year")
    public List<LongRidesPerYearDTO> getLongRidesPerYear() {
        log.debug("getLongRidesPerYear called");
        return rideActivityRepository.findLongRidesPerYear(LONG_RIDE_DISTANCE);
    }

    @GetMapping("/{fromDistance}/{toDistance}")
    public List<RideActivityDTO> getRidesBetween(@PathVariable int fromDistance, @PathVariable int toDistance) {
        log.debug("getRidesBetween called with fromDistance={}, toDistance={}", fromDistance, toDistance);
        return rideActivityRepository.findRidesBetween(fromDistance, toDistance).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/{fromDistance}")
    public List<RideActivityDTO> getRidesLongerThan(@PathVariable int fromDistance) {
        log.debug("getRidesLongerThan called with fromDistance={}", fromDistance);
        return rideActivityRepository.findRidesLongerThan(fromDistance).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/count/{fromDistance}/{toDistance}")
    public int getCountRidesBetween(@PathVariable int fromDistance, @PathVariable int toDistance) {
        log.debug("getCountRidesBetween called with fromDistance={}, toDistance={}", fromDistance, toDistance);
        return rideActivityRepository.countRidesBetween(fromDistance, toDistance);
    }

    @GetMapping("/count/{fromDistance}")
    public int getCountRidesLongerThan(@PathVariable int fromDistance) {
        log.debug("getCountRidesLongerThan called with fromDistance={}", fromDistance);
        return rideActivityRepository.countRidesLongerThan(fromDistance);
    }

    @GetMapping("/distance-range-counts")
    public List<DistanceRangeCountDTO> getDistanceRangeCounts() {
        log.debug("getDistanceRangeCounts called");
        return rideActivityRepository.findDistanceRangeCounts();
    }

    @GetMapping("/rides-by-bike")
    public List<RidesByBikeDTO> getRidesByBikeSinceDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceDate) {
        log.debug("getRidesByBikeSinceDate called with sinceDate={}", sinceDate);
        return rideActivityRepository.findRidesByBikeSinceDate(sinceDate);
    }

    @GetMapping("/earliest-ride-by-bike")
    public List<EarliestRideByBikeDTO> getEarliestRideByBike() {
        log.debug("getEarliestRideByBike called");
        return rideActivityRepository.findEarliestRideByBike();
    }

    @GetMapping("/odometer")
    public List<OdometerDTO> getOdometer() {
        log.debug("getOdometer called");
        return bikeRepository.findOdometer();
    }
}

