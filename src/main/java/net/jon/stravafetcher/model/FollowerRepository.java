package net.jon.stravafetcher.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    public Follower findByFirstNameAndLastName(String firstName, String lastName);
}
