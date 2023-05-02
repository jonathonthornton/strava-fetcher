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
    List<Integer> findAllDistancesInKilometersRoundedDown();

    @Query(value = "SELECT * FROM ride_activity ORDER BY start_date DESC LIMIT :n", nativeQuery = true)
    List<RideActivity> findMostRecentNRides(@Param("n") int n);

    @Query("SELECT r FROM RideActivity r WHERE r.distance >= :minDistance AND r.distance <= :maxDistance ORDER BY r.distance DESC")
    List<RideActivity> findRidesBetweenDistancesOrderedByDistanceDesc(@Param("minDistance") int minDistance, @Param("maxDistance") int maxDistance);

    @Query("SELECT COUNT(r) FROM RideActivity r WHERE r.distance >= :minDistance AND r.distance <= :maxDistance")
    int countRidesBetweenDistances(@Param("minDistance") int minDistance, @Param("maxDistance") int maxDistance);
}
