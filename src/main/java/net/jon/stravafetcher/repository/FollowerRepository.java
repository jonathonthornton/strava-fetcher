package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.Follower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    public Follower findByFirstNameAndLastName(String firstName, String lastName);
}
