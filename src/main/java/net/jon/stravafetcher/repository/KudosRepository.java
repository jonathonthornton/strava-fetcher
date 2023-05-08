package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.dto.FollowerDTO;
import net.jon.stravafetcher.model.Kudos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface KudosRepository extends JpaRepository<Kudos, Long> {
    @Query("SELECT new net.jon.stravafetcher.dto.FollowerDTO(f.firstName, f.lastName, COUNT(k)) " +
            "FROM Kudos k " +
            "JOIN k.follower f " +
            "JOIN RideActivity a ON a.id = k.activityId " +
            "WHERE a.startDateLocal >= :dateFrom " +
            "GROUP BY f.firstName, f.lastName " +
            "ORDER BY COUNT(k) DESC")
    Page<FollowerDTO> findTopKudosers(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);
}

