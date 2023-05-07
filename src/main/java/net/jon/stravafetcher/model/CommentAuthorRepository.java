package net.jon.stravafetcher.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentAuthorRepository extends JpaRepository<CommentAuthor, Long> {
    public CommentAuthor findByFirstNameAndLastName(String firstName, String lastName);
}
