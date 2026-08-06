package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.service.StravaOAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Web-based replacement for StravaClient's console/Scanner authorization
 * flow, which has no equivalent on a deployed host. Visit /oauth/authorize
 * once (after each deploy, or whenever the refresh token is invalidated) to
 * connect the app to Strava; the scheduled sync keeps the token current
 * after that via StravaOAuthService#refreshToken.
 */
@RestController
public class StravaOAuthCallbackController {
    private final StravaOAuthService stravaOAuthService;

    public StravaOAuthCallbackController(StravaOAuthService stravaOAuthService) {
        this.stravaOAuthService = stravaOAuthService;
    }

    @GetMapping("/oauth/authorize")
    public ResponseEntity<Void> authorize() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(stravaOAuthService.getAuthorizationUrl()))
                .build();
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        return stravaOAuthService.getOAuthToken(code)
                .map(token -> ResponseEntity.ok("Strava account connected. You can close this window."))
                .orElse(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Failed to exchange authorization code with Strava."));
    }
}
