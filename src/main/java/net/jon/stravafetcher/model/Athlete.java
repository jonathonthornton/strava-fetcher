package net.jon.stravafetcher.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Entity
@Table(name = "athlete")
public class Athlete {
    @Id
    private long id;

    @Column(name = "username")
    private String username;

    @Column(name = "resource_state")
    private int resourceState;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "sex")
    private String sex;

    @Column(name = "premium")
    private boolean premium;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "badge_type_id")
    private int badgeTypeId;

    @Column(name = "profile_medium")
    private String profileMedium;

    @Column(name = "profile")
    private String profile;

    @Column(name = "friend")
    private String friend;

    @Column(name = "follower")
    private String follower;

    @Column(name = "follower_count")
    private int followerCount;

    @Column(name = "friend_count")
    private int friendCount;

    @Column(name = "mutual_friend_count")
    private int mutualFriendCount;

    @Column(name = "athlete_type")
    private int athleteType;

    @Column(name = "date_preference")
    private String datePreference;

    @Column(name = "measurement_preference")
    private String measurementPreference;

    @Column(name = "ftp")
    private Integer ftp;

    @Column(name = "weight")
    private double weight;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "athlete")
    private List<Bike> bikes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getResourceState() {
        return resourceState;
    }

    public void setResourceState(int resourceState) {
        this.resourceState = resourceState;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.createdAt = LocalDateTime.parse(createdAt, formatter);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.updatedAt = LocalDateTime.parse(updatedAt, formatter);
    }

    public int getBadgeTypeId() {
        return badgeTypeId;
    }

    public void setBadgeTypeId(int badgeTypeId) {
        this.badgeTypeId = badgeTypeId;
    }

    public String getProfileMedium() {
        return profileMedium;
    }

    public void setProfileMedium(String profileMedium) {
        this.profileMedium = profileMedium;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(int friendCount) {
        this.friendCount = friendCount;
    }

    public int getMutualFriendCount() {
        return mutualFriendCount;
    }

    public void setMutualFriendCount(int mutualFriendCount) {
        this.mutualFriendCount = mutualFriendCount;
    }

    public int getAthleteType() {
        return athleteType;
    }

    public void setAthleteType(int athleteType) {
        this.athleteType = athleteType;
    }

    public String getDatePreference() {
        return datePreference;
    }

    public void setDatePreference(String datePreference) {
        this.datePreference = datePreference;
    }

    public String getMeasurementPreference() {
        return measurementPreference;
    }

    public void setMeasurementPreference(String measurementPreference) {
        this.measurementPreference = measurementPreference;
    }

    public Integer getFtp() {
        return ftp;
    }

    public void setFtp(Integer ftp) {
        this.ftp = ftp;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<Bike> getBikes() {
        return bikes;
    }

    public void setBikes(List<Bike> bikes) {
        this.bikes = bikes;
    }
}
