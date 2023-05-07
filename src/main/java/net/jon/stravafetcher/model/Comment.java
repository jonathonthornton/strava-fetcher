package net.jon.stravafetcher.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "comment")
public class Comment {

    @Id
    private long id;

    @Column(name = "activity_id")
    private long activityId;

    @Column(name = "text")
    private String text;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "first_name", referencedColumnName = "first_name"),
            @JoinColumn(name = "last_name", referencedColumnName = "last_name")
    })
    private CommentAuthor athlete;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text.substring(0, Math.min(text.length(), 255));
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.createdAt = LocalDateTime.parse(createdAt, formatter);
    }

    public CommentAuthor getAthlete() {
        return athlete;
    }

    public void setAthlete(CommentAuthor athlete) {
        this.athlete = athlete;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", text='" + text + '\'' +
                ", createdAt=" + createdAt +
                ", athlete=" + athlete +
                '}';
    }
}

