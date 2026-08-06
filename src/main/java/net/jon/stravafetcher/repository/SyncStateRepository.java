package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.SyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncStateRepository extends JpaRepository<SyncState, Long> {
}
