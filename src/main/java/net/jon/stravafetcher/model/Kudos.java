package net.jon.stravafetcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "kudos")
@IdClass(Kudos.KudosId.class)
public class Kudos {

    @Id
    @Column(name = "activity_id")
    private long activityId;

    @Id
    @ManyToOne
    @JoinColumn(name = "follower_id")
    @JsonProperty("athlete")
    private Follower follower;

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public Follower getFollower() {
        return follower;
    }

    public void setFollower(Follower follower) {
        this.follower = follower;
    }

    public static class KudosId implements Serializable {
        private long activityId;
        private long follower;

        public KudosId() {
        }

        public KudosId(long activityId, long follower) {
            this.activityId = activityId;
            this.follower = follower;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KudosId kudosId = (KudosId) o;
            return activityId == kudosId.activityId && follower == kudosId.follower;
        }

        @Override
        public int hashCode() {
            return Objects.hash(activityId, follower);
        }
    }
}
