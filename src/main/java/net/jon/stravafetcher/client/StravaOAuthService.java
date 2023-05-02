package net.jon.stravafetcher.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class StravaOAuthService {
    private static final Logger log = LoggerFactory.getLogger(StravaOAuthService.class);
    private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestTemplate restTemplate;

    public StravaOAuthService(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.restTemplate = new RestTemplate();
    }

    public OAuthTokenResponse getAccessToken(String authorizationCode) {
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
        ResponseEntity<OAuthTokenResponse> response = restTemplate.exchange(
                STRAVA_TOKEN_URL,
                HttpMethod.POST,
                request,
                OAuthTokenResponse.class);
        log.debug("response {}", response);
        return response.getBody();
    }

    public OAuthTokenResponse refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OAuthTokenResponse> responseEntity = restTemplate.exchange(
                STRAVA_TOKEN_URL,
                HttpMethod.POST,
                requestEntity,
                OAuthTokenResponse.class);

        return responseEntity.getBody();
    }
}
