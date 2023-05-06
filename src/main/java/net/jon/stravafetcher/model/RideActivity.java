package net.jon.stravafetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "ride_activity")
public class RideActivity {
    @Id
    private long id;

    @JsonProperty("start_latlng")
    @ElementCollection
    @CollectionTable(name = "ride_activity_start_latlng", joinColumns = @JoinColumn(name = "ride_activity_id"))
    @Column(name = "start_latlng")
    private List<Double> startLatLng;

    @JsonProperty("end_latlng")
    @ElementCollection
    @CollectionTable(name = "ride_activity_end_latlng", joinColumns = @JoinColumn(name = "ride_activity_id"))
    @Column(name = "end_latlng")
    private List<Double> endLatLng;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "activity_map_id")
    private ActivityMap map;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id")
    private Athlete athlete;

    @Column(nullable = false)
    private String name;

    @Column(name = "distance")
    private double distance;

    @Column(name = "moving_time")
    private int movingTime;

    @Column(name = "elapsed_time")
    private int elapsedTime;

    @Column(name = "total_elevation_gain")
    private double totalElevationGain;

    @Column(name = "sport_type")
    private String sportType;

    @Column(name = "start_date_local")
    private LocalDateTime startDateLocal;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "location_country")
    private String locationCountry;

    @Column(name = "achievement_count")
    private int achievementCount;

    @Column(name = "kudos_count")
    private int kudosCount;

    @Column(name = "comment_count")
    private int commentCount;

    @Column(name = "athlete_count")
    private int athleteCount;

    @Column(name = "photo_count")
    private int photoCount;

    @Column(name = "trainer")
    private boolean trainer;

    @Column(name = "commute")
    private boolean commute;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Column(name = "flagged")
    private boolean flagged;

    @Column(name = "gear_id")
    private String gearId;

    @Column(name = "average_speed")
    private double averageSpeed;

    @Column(name = "max_speed")
    private double maxSpeed;

    @Column(name = "average_cadence")
    private double averageCadence;

    @Column(name = "average_watts")
    private double averageWatts;

    @Column(name = "weighted_average_watts")
    private int weightedAverageWatts;

    @Column(name = "kilojoules")
    private double kilojoules;

    @Column(name = "device_watts")
    private boolean deviceWatts;

    @Column(name = "has_heartrate")
    private boolean hasHeartrate;

    @Column(name = "average_heartrate")
    private double averageHeartrate;

    @Column(name = "max_heartrate")
    private int maxHeartrate;

    @Column(name = "max_watts")
    private int maxWatts;

    @Column(name = "pr_count")
    private int prCount;

    @Column(name = "total_photo_count")
    private int totalPhotoCount;

    @Column(name = "suffer_score")
    private int sufferScore;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Double> getStartLatLng() {
        return startLatLng;
    }

    public void setStartLatLng(List<Double> startLatLng) {
        this.startLatLng = startLatLng;
    }

    public List<Double> getEndLatLng() {
        return endLatLng;
    }

    public void setEndLatLng(List<Double> endLatLng) {
        this.endLatLng = endLatLng;
    }

//    @JsonIgnore
    public ActivityMap getMap() {
        return map;
    }

    public void setMap(ActivityMap map) {
        this.map = map;
    }

    //    @JsonIgnore
    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
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
        this.distance = distance / 1000;
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

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public LocalDateTime getStartDateLocal() {
        return startDateLocal;
    }

    public void setStartDateLocal(String startDateLocal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.startDateLocal = LocalDateTime.parse(startDateLocal, formatter);
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = parseTimezone(timezone);
    }

    public String getLocationCountry() {
        return locationCountry;
    }

    public void setLocationCountry(String locationCountry) {
        this.locationCountry = locationCountry;
    }

    public int getAchievementCount() {
        return achievementCount;
    }

    public void setAchievementCount(int achievementCount) {
        this.achievementCount = achievementCount;
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

    public int getAthleteCount() {
        return athleteCount;
    }

    public void setAthleteCount(int athleteCount) {
        this.athleteCount = athleteCount;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public boolean isTrainer() {
        return trainer;
    }

    public void setTrainer(boolean trainer) {
        this.trainer = trainer;
    }

    public boolean isCommute() {
        return commute;
    }

    public void setCommute(boolean commute) {
        this.commute = commute;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getGearId() {
        return gearId;
    }

    public void setGearId(String gearId) {
        this.gearId = gearId;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = metersPerSecondToKilometersPerHour(averageSpeed);
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = metersPerSecondToKilometersPerHour(maxSpeed);
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

    public boolean isDeviceWatts() {
        return deviceWatts;
    }

    public void setDeviceWatts(boolean deviceWatts) {
        this.deviceWatts = deviceWatts;
    }

    public boolean isHasHeartrate() {
        return hasHeartrate;
    }

    public void setHasHeartrate(boolean hasHeartrate) {
        this.hasHeartrate = hasHeartrate;
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

    public int getPrCount() {
        return prCount;
    }

    public void setPrCount(int prCount) {
        this.prCount = prCount;
    }

    public int getTotalPhotoCount() {
        return totalPhotoCount;
    }

    public void setTotalPhotoCount(int totalPhotoCount) {
        this.totalPhotoCount = totalPhotoCount;
    }

    public int getSufferScore() {
        return sufferScore;
    }

    public void setSufferScore(int sufferScore) {
        this.sufferScore = sufferScore;
    }

    public String getBike() {
        return athlete.getBikes().stream()
                .filter(bike -> bike.getId().equals(gearId))
                .map(Bike::getName)
                .findFirst()
                .orElse("Unknown");
    }

    private double metersPerSecondToKilometersPerHour(double metersPerSecond) {
        return (metersPerSecond * 3600) / 1000;
    }

    private String parseTimezone(String timezone) {
        String timeZonePattern = "GMT[+-]\\d{2}:\\d{2}";
        Pattern pattern = Pattern.compile(timeZonePattern);
        Matcher matcher = pattern.matcher(timezone);
        if (matcher.find()) {
            return matcher.group();
        }
        return "GMT+00:00";
    }
}
