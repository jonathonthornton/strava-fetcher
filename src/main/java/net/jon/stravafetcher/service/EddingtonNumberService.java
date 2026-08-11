package net.jon.stravafetcher.service;

import net.jon.stravafetcher.repository.RideActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EddingtonNumberService {
    private static final int MAX_DISTANCES = 300;

    private final RideActivityRepository rideActivityRepository;

    public EddingtonNumberService(RideActivityRepository rideActivityRepository) {
        this.rideActivityRepository = rideActivityRepository;
    }

    public  int calculateEddingtonNumber() {
        List<Integer> dailyDistances = rideActivityRepository.findDistancesDesc(MAX_DISTANCES);

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

