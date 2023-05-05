package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.RideActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideActivityRepository extends JpaRepository<RideActivity, Long> {
    @Query("SELECT CAST(r.distance AS int) FROM RideActivity r")
    List<Integer> findAllDistances();

    @Query(value = "SELECT * FROM ride_activity ORDER BY start_date_local DESC LIMIT :n", nativeQuery = true)
    List<RideActivity> findRecentRides(@Param("n") int n);

    @Query("SELECT r FROM RideActivity r WHERE r.distance >= :minDistance AND r.distance <= :maxDistance ORDER BY r.distance DESC")
    List<RideActivity> findRidesBetween(@Param("minDistance") int minDistance, @Param("maxDistance") int maxDistance);

    @Query("SELECT COUNT(r) FROM RideActivity r WHERE r.distance >= :minDistance AND r.distance <= :maxDistance")
    int countRidesBetween(@Param("minDistance") int minDistance, @Param("maxDistance") int maxDistance);

    @Query("SELECT ra FROM RideActivity ra WHERE ra.startDateLocal = (SELECT MAX(ra2.startDateLocal) FROM RideActivity ra2)")
    RideActivity findMostRecentRideActivity();
}
