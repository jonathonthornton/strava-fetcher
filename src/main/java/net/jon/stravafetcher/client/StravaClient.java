package net.jon.stravafetcher.client;

import net.jon.stravafetcher.repository.KudosRepository;
import net.jon.stravafetcher.service.FetchService;
import net.jon.stravafetcher.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StravaClient {
    private static final String STRAVA_AUTHORIZE_URL = "https://www.strava.com/oauth/authorize";
    private static final String AUTHORIZATION_SCOPE = "activity:read,activity:read_all,profile:read_all";
    private static final Logger log = LoggerFactory.getLogger(StravaClient.class);
    @Value("${strava.client.id}")
    private String clientId;
    @Value("${strava.client.secret}")
    private String clientSecret;
    @Value("${strava.redirect.uri}")
    private String redirectUri;
    private final FetchService fetchService;
    private final KudosRepository kudosRepository;
    private final CommentRepository commentRepository;

    public StravaClient(FetchService fetchService, CommentRepository commentRepository, KudosRepository kudosRepository) {
        this.fetchService = fetchService;
        this.commentRepository = commentRepository;
        this.kudosRepository = kudosRepository;
    }

    public void fetchActivities() {
        String accessToken = getAccessToken();
        fetchService.fetchAthlete(accessToken);
        fetchService.fetchRecentActivities(accessToken);
        fetchService.fetchKudos(accessToken);
        fetchService.fetchComments(accessToken);

        kudosRepository.findTopKudosers(
                        LocalDateTime.now().minusYears(1),
                        PageRequest.of(0, 10))
                .forEach(follower -> {
                    log.info("Top kudoser: {}", follower);
                });

        kudosRepository.findBottomKudosers(
                        LocalDateTime.now().minusYears(1),
                        PageRequest.of(0, 10))
                .forEach(follower -> {
                    log.info("Bottom kudoser: {}", follower);
                });

        commentRepository.findTopCommenters(
                        LocalDateTime.now().minusYears(1),
                        PageRequest.of(0, 10))
                .forEach(follower -> {
                    log.info("Top commenter: {}", follower);
                });
    }

    private String getAccessToken() {
        String authorizationUrl = buildAuthorizationUrl();
        String authorizationCode = getAuthorizationCode(authorizationUrl);
        StravaOAuthService stravaOAuthService = new StravaOAuthService(clientId, clientSecret, redirectUri);
        OAuthTokenResponse tokenResponse = stravaOAuthService.getAccessToken(authorizationCode);
        return tokenResponse.getAccessToken();
    }

    private String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(STRAVA_AUTHORIZE_URL)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", AUTHORIZATION_SCOPE)
                .toUriString();
    }

    private String getAuthorizationCode(String authorizationUrl) {
        try {
            System.out.println("Please open the following URL in your browser, authorize the application, and then paste the redirected URL back here:");
            System.out.println(authorizationUrl);
            Scanner scanner = new Scanner(System.in);
            String redirectedUrl = scanner.nextLine();
            return extractAuthorizationCode(redirectedUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error getting the authorization code", e);
        }
    }

    private String extractAuthorizationCode(String redirectedUrl) {
        Matcher matcher = Pattern.compile("code=([^&]+)").matcher(redirectedUrl);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Authorization code not found in the redirected URL");
        }
    }
}

