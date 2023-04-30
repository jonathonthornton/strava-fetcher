package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.RideActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideActivityRepository extends JpaRepository<RideActivity, Long> {
    // Additional query methods can be added as needed
}
