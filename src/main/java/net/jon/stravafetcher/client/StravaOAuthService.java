package net.jon.stravafetcher.client;

import net.jon.stravafetcher.model.OAuthToken;
import net.jon.stravafetcher.repository.OAuthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class StravaOAuthService {
    private static final Logger log = LoggerFactory.getLogger(StravaOAuthService.class);
    private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String STRAVA_AUTHORIZE_URL = "https://www.strava.com/oauth/authorize";
    private static final String AUTHORIZATION_SCOPE = "activity:read,activity:read_all,profile:read_all";
    private final OAuthTokenRepository oAuthTokenRepository;
    @Value("${strava.client.id}")
    private String clientId;
    @Value("${strava.client.secret}")
    private String clientSecret;
    @Value("${strava.redirect.uri}")
    private String redirectUri;

    public StravaOAuthService(OAuthTokenRepository oAuthTokenRepository) {
        this.oAuthTokenRepository = oAuthTokenRepository;
    }

    public Optional<OAuthToken> readOAuthToken() {
        return oAuthTokenRepository.findAll().stream().findFirst();
    }

    public boolean isTokenCurrent() {
        Optional<OAuthToken> token = readOAuthToken();
        if (token.isPresent()) {
            Instant instant = Instant.ofEpochSecond(token.get().getExpiresAt());
            LocalDateTime expiresAt = LocalDateTime.ofInstant(instant, ZoneId.of("Australia/Melbourne"));
            log.debug("expiresAt {}", expiresAt);
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Australia/Melbourne"));
            return expiresAt.isAfter(now);
        } else {
            return false;
        }
    }

    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(STRAVA_AUTHORIZE_URL)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", AUTHORIZATION_SCOPE)
                .toUriString();
    }

    public Optional<OAuthToken> getOAuthToken(String authorizationCode) {
        log.debug("authorizationCode {}", authorizationCode);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("code", authorizationCode);
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("redirect_uri", redirectUri);
        log.debug("requestBody {}", requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OAuthToken> responseEntity = restTemplate.exchange(
                STRAVA_TOKEN_URL,
                HttpMethod.POST,
                request,
                OAuthToken.class);

        if (responseEntity.getBody() != null) {
            oAuthTokenRepository.deleteAll();
            return Optional.of(oAuthTokenRepository.save(responseEntity.getBody()));
        }
        return Optional.empty();
    }

    public Optional<OAuthToken> refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OAuthToken> responseEntity = restTemplate.exchange(
                STRAVA_TOKEN_URL,
                HttpMethod.POST,
                requestEntity,
                OAuthToken.class);

        if (responseEntity.getBody() != null) {
            oAuthTokenRepository.deleteAll();
            return Optional.of(oAuthTokenRepository.save(responseEntity.getBody()));
        }
        return Optional.empty();
    }
}
