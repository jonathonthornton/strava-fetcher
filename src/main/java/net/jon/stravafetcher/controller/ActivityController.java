package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.client.mapper.RideActivityMapper;
import net.jon.stravafetcher.dto.RideActivityDTO;
import net.jon.stravafetcher.repository.RideActivityRepository;
import net.jon.stravafetcher.service.EddingtonNumberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {
    private final RideActivityRepository rideActivityRepository;
    private final EddingtonNumberService eddingtonNumberService;

    public ActivityController(RideActivityRepository rideActivityRepository, EddingtonNumberService eddingtonNumberService) {
        this.rideActivityRepository = rideActivityRepository;
        this.eddingtonNumberService = eddingtonNumberService;
    }

    @GetMapping("/count")
    public long getActivityCount() {
        return rideActivityRepository.count();
    }

    @GetMapping("/eddington-number")
    public int getEddingtonNumber() {
        return eddingtonNumberService.calculateEddingtonNumber();
    }

    @GetMapping("/recent/{limit}")
    public List<RideActivityDTO> getRecentRides(@PathVariable int limit) {
        return rideActivityRepository.findRecentRides(limit).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/{fromDistance}/{toDistance}")
    public List<RideActivityDTO> getRidesBetween(@PathVariable int fromDistance, @PathVariable int toDistance) {
        return rideActivityRepository.findRidesBetween(fromDistance, toDistance).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/{fromDistance}")
    public List<RideActivityDTO> getRidesLongerThan(@PathVariable int fromDistance) {
        return rideActivityRepository.findRidesLongerThan(fromDistance).stream()
                .map(RideActivityMapper::mapToDTO)
                .toList();
    }

    @GetMapping("/count/{fromDistance}/{toDistance}")
    public int getCountRidesBetween(@PathVariable int fromDistance, @PathVariable int toDistance) {
        return rideActivityRepository.countRidesBetween(fromDistance, toDistance);
    }

    @GetMapping("/count/{fromDistance}")
    public int getCountRidesLongerThan(@PathVariable int fromDistance) {
        return rideActivityRepository.countRidesLongerThan(fromDistance);
    }
}

