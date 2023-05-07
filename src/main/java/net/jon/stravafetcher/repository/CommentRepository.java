package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.dto.CommenterDTO;
import net.jon.stravafetcher.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT new net.jon.stravafetcher.dto.CommenterDTO(c.commentAuthor.firstName, c.commentAuthor.lastName, COUNT(c)) " +
            "FROM Comment c " +
            "WHERE c.createdAt >= :dateFrom " +
            "GROUP BY c.commentAuthor.firstName, c.commentAuthor.lastName " +
            "ORDER BY COUNT(c) DESC")
    List<CommenterDTO> findTopCommenters(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);
}
