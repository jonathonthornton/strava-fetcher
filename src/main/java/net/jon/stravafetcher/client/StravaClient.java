package net.jon.stravafetcher.client;

import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.RideActivity;
import net.jon.stravafetcher.repository.AthleteRepository;
import net.jon.stravafetcher.service.StravaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jon.stravafetcher.repository.RideActivityRepository;

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

    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private RideActivityRepository rideActivityRepository;

    @Autowired
    private StravaService stravaService;

    private  String getAccessToken() {
        String authorizationUrl = buildAuthorizationUrl();
        String authorizationCode = getAuthorizationCode(authorizationUrl);
        StravaOAuthService stravaOAuthService = new StravaOAuthService(clientId, clientSecret, redirectUri);
        OAuthTokenResponse tokenResponse = stravaOAuthService.getAccessToken(authorizationCode);
        return tokenResponse.getAccessToken();
    }

    public void fetchActivities() {
        for (int page = 1; page <= 2; page++) {
            List<RideActivity> activities = stravaService.getActivities(getAccessToken(), page, 10);

            activities.forEach((activity) -> {
                if (!athleteRepository.existsById((long) activity.getAthlete().getId())) {
                    athleteRepository.save(activity.getAthlete());
                }
                if (!rideActivityRepository.existsById(activity.getId())) {
                    rideActivityRepository.save(activity);
                }
            });
            log.debug("Page {} fetched and saved", page);
        }
        log.debug("Activities fetched and saved");
    }

    public void fetchAthlete() {
        Athlete athlete = stravaService.getAthlete(getAccessToken());
        if (!athleteRepository.existsById((long) athlete.getId())) {
            athleteRepository.save(athlete);
        }
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

