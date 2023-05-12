package net.jon.stravafetcher.repository;

import net.jon.stravafetcher.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, String> {
}
