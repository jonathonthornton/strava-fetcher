package net.jon.stravafetcher;

import net.jon.stravafetcher.model.RideActivity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class StravaService {
    private static final String STRAVA_API_BASE_URL = "https://www.strava.com/api/v3";

    private final String accessToken;
    private final RestTemplate restTemplate;

    public StravaService(String accessToken) {
        this.accessToken = accessToken;
        this.restTemplate = new RestTemplate();
    }

    public List<RideActivity> getActivities() {
        String url = STRAVA_API_BASE_URL + "/athlete/activities";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<RideActivity[]> response = restTemplate.exchange(url, HttpMethod.GET, request, RideActivity[].class);

        return Arrays.asList(response.getBody());
    }
}
