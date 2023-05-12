package net.jon.stravafetcher.mapper;

import net.jon.stravafetcher.dto.RideActivityDTO;
import net.jon.stravafetcher.model.RideActivity;

public class RideActivityMapper {
    public static RideActivityDTO mapToDTO(RideActivity rideActivity) {
        RideActivityDTO rideActivityDTO = new RideActivityDTO();

        rideActivityDTO.setId(rideActivity.getId());
        rideActivityDTO.setName(rideActivity.getName());
        rideActivityDTO.setDistance(rideActivity.getDistance());
        rideActivityDTO.setMovingTime(rideActivity.getMovingTime());
        rideActivityDTO.setElapsedTime(rideActivity.getElapsedTime());
        rideActivityDTO.setTotalElevationGain(rideActivity.getTotalElevationGain());
        rideActivityDTO.setStartDateLocal(rideActivity.getStartDateLocal());
        rideActivityDTO.setKudosCount(rideActivity.getKudosCount());
        rideActivityDTO.setCommentCount(rideActivity.getCommentCount());
        rideActivityDTO.setAverageSpeed(rideActivity.getAverageSpeed());
        rideActivityDTO.setMaxSpeed(rideActivity.getMaxSpeed());
        rideActivityDTO.setAverageCadence(rideActivity.getAverageCadence());
        rideActivityDTO.setAverageWatts(rideActivity.getAverageWatts());
        rideActivityDTO.setWeightedAverageWatts(rideActivity.getWeightedAverageWatts());
        rideActivityDTO.setKilojoules(rideActivity.getKilojoules());
        rideActivityDTO.setAverageHeartrate(rideActivity.getAverageHeartrate());
        rideActivityDTO.setMaxHeartrate(rideActivity.getMaxHeartrate());
        rideActivityDTO.setMaxWatts(rideActivity.getMaxWatts());
        rideActivityDTO.setSufferScore(rideActivity.getSufferScore());
        rideActivityDTO.setBike(rideActivity.getBike());

        return rideActivityDTO;
    }
}
