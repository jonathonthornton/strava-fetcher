package net.jon.stravafetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "bike")
public class Bike {
    @Id
    private String id;

    @Column(name = "primary_bike")
    private boolean primary;

    @Column(name = "name")
    private String name;

    @Column(name = "resource_state")
    private int resourceState;

    @Column(name = "distance")
    private double distance;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "athlete_id")
    private Athlete athlete;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResourceState() {
        return resourceState;
    }

    public void setResourceState(int resourceState) {
        this.resourceState = resourceState;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance / 1000;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "id='" + id + '\'' +
                ", primary=" + primary +
                ", name='" + name + '\'' +
                ", resourceState=" + resourceState +
                ", distance=" + distance +
                ", athlete=" + athlete +
                '}';
    }
}
