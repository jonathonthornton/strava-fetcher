package net.jon.stravafetcher.model;

import java.util.List;

public class RideActivity {
    private int resourceState;
    private Athlete athlete;
    private String name;
    private double distance;
    private int movingTime;
    private int elapsedTime;
    private double totalElevationGain;
    private String type;
    private String sportType;
    private String workoutType;
    private long id;
    private String externalId;
    private long uploadId;
    private String startDate;
    private String startDateLocal;
    private String timezone;
    private int utcOffset;
    private List<Double> startLatLng;
    private List<Double> endLatLng;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private int achievementCount;
    private int kudosCount;
    private int commentCount;
    private int athleteCount;
    private int photoCount;
    private ActivityMap map;
    private boolean trainer;
    private boolean commute;
    private boolean manual;
    private boolean isPrivate;
    private boolean flagged;
    private String gearId;
    private boolean fromAcceptedTag;
    private double averageSpeed;
    private double maxSpeed;
    private double averageCadence;
    private double averageWatts;
    private int weightedAverageWatts;
    private double kilojoules;
    private boolean deviceWatts;
    private boolean hasHeartrate;
    private double averageHeartrate;
    private int maxHeartrate;
    private int maxWatts;
    private int prCount;
    private int totalPhotoCount;
    private boolean hasKudoed;
    private int sufferScore;

    public int getResourceState() {
        return resourceState;
    }

    public void setResourceState(int resourceState) {
        this.resourceState = resourceState;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public long getUploadId() {
        return uploadId;
    }

    public void setUploadId(long uploadId) {
        this.uploadId = uploadId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartDateLocal() {
        return startDateLocal;
    }

    public void setStartDateLocal(String startDateLocal) {
        this.startDateLocal = startDateLocal;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(int utcOffset) {
        this.utcOffset = utcOffset;
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

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getLocationState() {
        return locationState;
    }

    public void setLocationState(String locationState) {
        this.locationState = locationState;
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

    public ActivityMap getMap() {
        return map;
    }

    public void setMap(ActivityMap map) {
        this.map = map;
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

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
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

    public boolean isFromAcceptedTag() {
        return fromAcceptedTag;
    }

    public void setFromAcceptedTag(boolean fromAcceptedTag) {
        this.fromAcceptedTag = fromAcceptedTag;
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

    public boolean isHasKudoed() {
        return hasKudoed;
    }

    public void setHasKudoed(boolean hasKudoed) {
        this.hasKudoed = hasKudoed;
    }

    public int getSufferScore() {
        return sufferScore;
    }

    public void setSufferScore(int sufferScore) {
        this.sufferScore = sufferScore;
    }

    public static class Athlete {
        private int id;
        private int resourceState;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getResourceState() {
            return resourceState;
        }

        public void setResourceState(int resourceState) {
            this.resourceState = resourceState;
        }
    }

    public static class ActivityMap {
        private String id;
        private String summaryPolyline;
        private int resourceState;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSummaryPolyline() {
            return summaryPolyline;
        }

        public void setSummaryPolyline(String summaryPolyline) {
            this.summaryPolyline = summaryPolyline;
        }

        public int getResourceState() {
            return resourceState;
        }

        public void setResourceState(int resourceState) {
            this.resourceState = resourceState;
        }
    }
}
