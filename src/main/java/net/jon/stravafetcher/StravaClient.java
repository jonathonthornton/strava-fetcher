package net.jon.stravafetcher;

import net.jon.stravafetcher.model.OAuthTokenResponse;
import net.jon.stravafetcher.model.RideActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StravaClient {
    private static final String STRAVA_AUTHORIZE_URL = "https://www.strava.com/oauth/authorize";
    private static final String AUTHORIZATION_SCOPE = "activity:read,activity:read_all";
    private static final Logger log = LoggerFactory.getLogger(StravaClient.class);
    @Value("${strava.client.id}")
    private String clientId;

    @Value("${strava.client.secret}")
    private String clientSecret;

    @Value("${strava.redirect.uri}")
    private String redirectUri;

    public void authorizeAndFetchActivities() {
        String authorizationUrl = buildAuthorizationUrl();
        String authorizationCode = getAuthorizationCode(authorizationUrl);
        StravaOAuthService stravaOAuthService = new StravaOAuthService(clientId, clientSecret, redirectUri);
        OAuthTokenResponse tokenResponse = stravaOAuthService.getAccessToken(authorizationCode);

        StravaService stravaService = new StravaService(tokenResponse.getAccessToken());
        List<RideActivity> activities = stravaService.getActivities();

        activities.forEach(activity -> System.out.println(activity.getName() + " - " + activity.getDistance()));
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

