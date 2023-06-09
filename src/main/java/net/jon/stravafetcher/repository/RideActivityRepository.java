package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.RideActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideActivityRepository extends JpaRepository<RideActivity, Long> {
    @Query(value = "SELECT CAST(r.distance AS int) FROM strava.ride_activity r", nativeQuery = true)
    List<Integer> findAllDistances();

    @Query(value = "SELECT * FROM strava.ride_activity ORDER BY start_date_local DESC LIMIT :n", nativeQuery = true)
    List<RideActivity> findRecentRides(@Param("n") int n);

    @Query(value = "SELECT * FROM strava.ride_activity r WHERE r.distance >= :minDistance AND r.distance < :maxDistance ORDER BY r.distance DESC", nativeQuery = true)
    List<RideActivity> findRidesBetween(@Param("minDistance") int minDistance, @Param("maxDistance") int maxDistance);

    @Query(value = "SELECT COUNT(*) FROM strava.ride_activity r WHERE r.distance >= :minDistance AND r.distance < :maxDistance", nativeQuery = true)
    int countRidesBetween(@Param("minDistance") int minDistance, @Param("maxDistance") int maxDistance);

    @Query(value = "SELECT * FROM strava.ride_activity r WHERE r.distance >= :minDistance ORDER BY r.distance DESC", nativeQuery = true)
    List<RideActivity> findRidesLongerThan(@Param("minDistance") int minDistance);

    @Query(value = "SELECT COUNT(*) FROM strava.ride_activity r WHERE r.distance >= :minDistance", nativeQuery = true)
    int countRidesLongerThan(@Param("minDistance") int minDistance);

    @Query(value = "SELECT * FROM strava.ride_activity ra WHERE ra.start_date_local = (SELECT MAX(ra2.start_date_local) FROM strava.ride_activity ra2)", nativeQuery = true)
    RideActivity findNewestRideActivity();

    @Query(value = "SELECT * FROM strava.ride_activity ra WHERE ra.start_date_local = (SELECT MIN(ra2.start_date_local) FROM strava.ride_activity ra2)", nativeQuery = true)
    Optional<RideActivity> findOldestRideActivity();

    @Query("SELECT r FROM RideActivity r WHERE r.id NOT IN (SELECT DISTINCT c.activityId FROM Comment c)")
    List<RideActivity> findActivitiesWithoutComments();

    @Query("SELECT r FROM RideActivity r WHERE r.startDateLocal BETWEEN :after AND :before")
    List<RideActivity> findActivitiesBetween(@Param("after") LocalDateTime after, @Param("before") LocalDateTime before);

    @Query("SELECT r " +
            "FROM RideActivity r " +
            "LEFT JOIN Kudos k ON r.id = k.activityId " +
            "WHERE r.isPrivate = false " +
            "GROUP BY r.id " +
            "HAVING COUNT(k) < r.kudosCount - 3") // Allow for name collisions.
    List<RideActivity> findPublicActivitiesWithMismatchedKudosCounts();

    @Query("SELECT r " +
            "FROM RideActivity r " +
            "LEFT JOIN Comment c ON r.id = c.activityId " +
            "WHERE r.isPrivate = false " +
            "GROUP BY r.id " +
            "HAVING COUNT(c) < r.commentCount - 3") // Allow for name collisions.
    List<RideActivity> findPublicActivitiesWithMismatchedCommentCounts();
}
