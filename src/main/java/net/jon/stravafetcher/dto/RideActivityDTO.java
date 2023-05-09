package net.jon.stravafetcher.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class RideActivityDTO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private long id;
    private String name;
    private double distance;
    private int movingTime;
    private int elapsedTime;
    private double totalElevationGain;
    private LocalDateTime startDateLocal;
    private int kudosCount;
    private int commentCount;
    private double averageSpeed;
    private double maxSpeed;
    private double averageCadence;
    private double averageWatts;
    private int weightedAverageWatts;
    private double kilojoules;
    private double averageHeartrate;
    private int maxHeartrate;
    private int maxWatts;
    private int sufferScore;
    private String bike;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getMovingTime() {
        return movingTime;
    }

    public void setMovingTime(int movingTime) {
        this.movingTime = movingTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public double getTotalElevationGain() {
        return totalElevationGain;
    }

    public void setTotalElevationGain(double totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }

    public String getStartDateLocal() {
        return startDateLocal.format(DATE_FORMATTER);
    }

    public void setStartDateLocal(LocalDateTime startDateLocal) {
        this.startDateLocal = startDateLocal;
    }

    public int getKudosCount() {
        return kudosCount;
    }

    public void setKudosCount(int kudosCount) {
        this.kudosCount = kudosCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getAverageCadence() {
        return averageCadence;
    }

    public void setAverageCadence(double averageCadence) {
        this.averageCadence = averageCadence;
    }

    public double getAverageWatts() {
        return averageWatts;
    }

    public void setAverageWatts(double averageWatts) {
        this.averageWatts = averageWatts;
    }

    public int getWeightedAverageWatts() {
        return weightedAverageWatts;
    }

    public void setWeightedAverageWatts(int weightedAverageWatts) {
        this.weightedAverageWatts = weightedAverageWatts;
    }

    public double getKilojoules() {
        return kilojoules;
    }

    public void setKilojoules(double kilojoules) {
        this.kilojoules = kilojoules;
    }

    public double getAverageHeartrate() {
        return averageHeartrate;
    }

    public void setAverageHeartrate(double averageHeartrate) {
        this.averageHeartrate = averageHeartrate;
    }

    public int getMaxHeartrate() {
        return maxHeartrate;
    }

    public void setMaxHeartrate(int maxHeartrate) {
        this.maxHeartrate = maxHeartrate;
    }

    public int getMaxWatts() {
        return maxWatts;
    }

    public void setMaxWatts(int maxWatts) {
        this.maxWatts = maxWatts;
    }

    public int getSufferScore() {
        return sufferScore;
    }

    public void setSufferScore(int sufferScore) {
        this.sufferScore = sufferScore;
    }

    public String getBike() {
        return bike;
    }

    public void setBike(String bike) {
        this.bike = bike;
    }
}

