package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.RideActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    RideActivity findMostRecentRideActivity();
}
