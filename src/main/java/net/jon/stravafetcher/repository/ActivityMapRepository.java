package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.ActivityMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityMapRepository extends JpaRepository<ActivityMap, Long> {
    // Additional query methods can be added as needed
}
