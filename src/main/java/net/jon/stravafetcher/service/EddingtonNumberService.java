package net.jon.stravafetcher.service;

import net.jon.stravafetcher.repository.RideActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class EddingtonNumberService {
    @Autowired
    private RideActivityRepository rideActivityRepository;

    public  int calculateEddingtonNumber() {
        List<Integer> dailyDistances = rideActivityRepository.findAllDistances();
        dailyDistances.sort(Collections.reverseOrder());

        int eddingtonNumber = 0;
        for (int i = 0; i < dailyDistances.size(); i++) {
            if (dailyDistances.get(i) >= i + 1) {
                eddingtonNumber = i + 1;
            } else {
                break;
            }
        }

        return eddingtonNumber;
    }
}

