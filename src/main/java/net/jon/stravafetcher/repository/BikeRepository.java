package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.Bike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    boolean existsById(String id);
}
