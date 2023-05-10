package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.dto.FollowerDTO;
import net.jon.stravafetcher.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT new net.jon.stravafetcher.dto.FollowerDTO(c.follower.firstName, c.follower.lastName, COUNT(c)) " +
            "FROM Comment c " +
            "JOIN RideActivity a ON a.id = c.activityId " +
            "WHERE a.startDateLocal >= :dateFrom " +
            "GROUP BY c.follower.firstName, c.follower.lastName " +
            "ORDER BY COUNT(c) DESC")
    Page<FollowerDTO> findTopCommenters(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);
}
