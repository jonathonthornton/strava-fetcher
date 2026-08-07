package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.dto.OdometerDTO;
import net.jon.stravafetcher.model.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    boolean existsById(String id);

    @Query("SELECT b.name AS name, b.distance AS distance FROM Bike b ORDER BY b.distance DESC")
    List<OdometerDTO> findOdometer();
}
