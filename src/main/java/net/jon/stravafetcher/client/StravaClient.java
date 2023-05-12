package net.jon.stravafetcher.client;

import net.jon.stravafetcher.model.OAuthToken;
import net.jon.stravafetcher.repository.KudosRepository;
import net.jon.stravafetcher.service.FetchService;
import net.jon.stravafetcher.repository.CommentRepository;
import net.jon.stravafetcher.service.StravaOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StravaClient {

    private static final Logger log = LoggerFactory.getLogger(StravaClient.class);
    private final StravaOAuthService stravaOAuthService;
    private final FetchService fetchService;
    private final KudosRepository kudosRepository;
    private final CommentRepository commentRepository;

    public StravaClient(StravaOAuthService stravaOAuthService, FetchService fetchService, CommentRepository commentRepository, KudosRepository kudosRepository){
        this.stravaOAuthService = stravaOAuthService;
        this.fetchService = fetchService;
        this.commentRepository = commentRepository;
        this.kudosRepository = kudosRepository;
    }

    public void fetchActivities() {
        Optional<OAuthToken> oAuthToken = getOAuthToken();
        if (oAuthToken.isPresent()) {
            String accessToken = oAuthToken.get().getAccessToken();
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
    }

    private Optional<OAuthToken> getOAuthToken() {
        Optional<OAuthToken> oAuthToken = stravaOAuthService.readOAuthToken();
        if (oAuthToken.isPresent() && stravaOAuthService.isTokenCurrent()) {
            log.info("Token found and is current");
        } else if (oAuthToken.isPresent()) {
            log.info("Token found but is not current. Refreshing token...");
            oAuthToken = stravaOAuthService.refreshToken(oAuthToken.get().getRefreshToken());
        } else {
            log.debug("No token found. Requesting a new one...");
            String authorizationUrl = stravaOAuthService.getAuthorizationUrl();
            String authorizationCode = getAuthorizationCode(authorizationUrl);
            oAuthToken = stravaOAuthService.getOAuthToken(authorizationCode);
        }
        return oAuthToken;
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

