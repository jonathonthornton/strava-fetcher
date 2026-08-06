package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.dto.DistanceRangeCountDTO;
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

    @Query(value = """
            SELECT distance_range AS "distanceRange", COUNT(*) AS "rideCount"
            FROM (SELECT CASE
                             WHEN distance > 0 AND distance < 50 THEN '0–49 km'
                             WHEN distance >= 50 AND distance < 100 THEN '50–99 km'
                             WHEN distance >= 100 AND distance < 200 THEN '100–199 km'
                             WHEN distance >= 200 AND distance < 300 THEN '200–299 km'
                             WHEN distance >= 300 AND distance < 400 THEN '300–399 km'
                             WHEN distance >= 400 AND distance < 600 THEN '400–599 km'
                             ELSE '600+ km'
                             END AS distance_range
                  FROM (SELECT round(CAST(ride_activity.distance AS numeric), 1) AS distance
                        FROM strava.ride_activity
                                 LEFT JOIN strava.bike ON ride_activity.gear_id = bike.id
                        WHERE sport_type IN ('Ride', 'MountainBikeRide', 'GravelRide')
                          AND round(CAST(ride_activity.distance AS numeric), 1) > 0) AS base) AS sub
            GROUP BY distance_range
            ORDER BY CASE distance_range
                         WHEN '0–49 km' THEN 1
                         WHEN '50–99 km' THEN 2
                         WHEN '100–199 km' THEN 3
                         WHEN '200–299 km' THEN 4
                         WHEN '300–399 km' THEN 5
                         WHEN '400–599 km' THEN 6
                         ELSE 7
                         END
            """, nativeQuery = true)
    List<DistanceRangeCountDTO> findDistanceRangeCounts();

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
            "HAVING COUNT(k) < r.kudosCount - 3 " + // Allow for name collisions.
            "ORDER BY r.startDateLocal DESC")
    List<RideActivity> findPublicActivitiesWithMismatchedKudosCounts();

    @Query("SELECT r " +
            "FROM RideActivity r " +
            "LEFT JOIN Comment c ON r.id = c.activityId " +
            "WHERE r.isPrivate = false " +
            "GROUP BY r.id " +
            "HAVING COUNT(c) < r.commentCount - 3 " + // Allow for name collisions.
            "ORDER BY r.startDateLocal DESC")
    List<RideActivity> findPublicActivitiesWithMismatchedCommentCounts();
}
