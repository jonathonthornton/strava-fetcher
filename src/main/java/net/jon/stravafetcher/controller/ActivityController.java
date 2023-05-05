package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.RideActivityRepository;
import net.jon.stravafetcher.service.EddingtonNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {
    @Autowired
    private RideActivityRepository rideActivityRepository;
    @Autowired
    private EddingtonNumberService eddingtonNumberService;

    @GetMapping("/count")
    public long getActivityCount() {
        return rideActivityRepository.count();
    }

    @GetMapping("/recent/{count}")
    public List<RideActivity> getRecentRides(@PathVariable int count) {
        return rideActivityRepository.findRecentRides(count);
    }

    @GetMapping("/{fromDistance}/{toDistance}")
    public List<RideActivity> getRidesBetween(@PathVariable int fromDistance, @PathVariable int toDistance) {
        return rideActivityRepository.findRidesBetween(fromDistance, toDistance);
    }

    @GetMapping("/count/{fromDistance}/{toDistance}")
    public int getCountBetween(@PathVariable int fromDistance, @PathVariable int toDistance) {
        return rideActivityRepository.countRidesBetween(fromDistance, toDistance);
    }

    @GetMapping("/eddington-number")
    public int getEddingtonNumber() {
        return eddingtonNumberService.calculateEddingtonNumber();
    }
}

