package net.jon.stravafetcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JoinColumn(name = "comment_author_id")
    @JsonProperty("athlete")
    private CommentAuthor commentAuthor;

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

    public CommentAuthor getCommentAuthor() {
        return commentAuthor;
    }

    public void setCommentAuthor(CommentAuthor commentAuthor) {
        this.commentAuthor = commentAuthor;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", text='" + text + '\'' +
                ", createdAt=" + createdAt +
                ", commentAuthor=" + commentAuthor +
                '}';
    }
}

