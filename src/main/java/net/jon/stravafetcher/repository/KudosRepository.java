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

    @Query("SELECT new net.jon.stravafetcher.dto.FollowerDTO(f.firstName, f.lastName, COALESCE(COUNT(k), 0)) " +
            "FROM Follower f " +
            "LEFT JOIN Kudos k ON f.id = k.follower.id " +
            "LEFT JOIN RideActivity a ON a.id = k.activityId " +
            "WHERE (k.activityId IS NULL OR a.startDateLocal >= :dateFrom) " +
            "GROUP BY f.id " +
            "HAVING COALESCE(COUNT(k), 0) = (" +
            "       SELECT MIN(subquery.kudos_count) " +
            "       FROM (" +
            "           SELECT f2.id AS follower_id, COALESCE(COUNT(k2), 0) AS kudos_count " +
            "           FROM Follower f2 " +
            "           LEFT JOIN Kudos k2 ON f2.id = k2.follower.id " +
            "           LEFT JOIN RideActivity a2 ON a2.id = k2.activityId " +
            "           WHERE (k2.activityId IS NULL OR a2.startDateLocal >= :dateFrom) " +
            "           GROUP BY f2.id" +
            "       ) AS subquery" +
            ") " +
            "ORDER BY COALESCE(COUNT(k), 0)")
    Page<FollowerDTO> findBottomKudosers(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);
}

