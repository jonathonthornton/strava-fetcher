package net.jon.stravafetcher.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "activity_map")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ActivityMap {
    private static final int MAX_POLYLINE_LENGTH = 5000;

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "summary_polyline", length = MAX_POLYLINE_LENGTH)
    private String summaryPolyline;

    @Column(name = "resource_state")
    private int resourceState;

    public ActivityMap() {}

    public ActivityMap(String id, String summaryPolyline, int resourceState) {
        this.id = id;
        this.summaryPolyline = summaryPolyline;
        this.resourceState = resourceState;
    }

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

