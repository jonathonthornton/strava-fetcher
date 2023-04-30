package net.jon.stravafetcher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.jon.stravafetcher.model.RideActivity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class StravaService {
    private static final String STRAVA_API_BASE_URL = "https://www.strava.com/api/v3";

    private final String accessToken;

    public StravaService(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<RideActivity> getActivities() {
        String url = STRAVA_API_BASE_URL + "/athlete/activities";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        // Fetch the raw JSON response as a String.
        ResponseEntity<String> rawJsonResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class);

        // Print the raw JSON response.
        System.out.println("Raw JSON Response: " + rawJsonResponse.getBody());

        // Deserialize the raw JSON response to a RideActivity[] array.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        RideActivity[] rideActivities = null;
        try {
            rideActivities = objectMapper.readValue(rawJsonResponse.getBody(), RideActivity[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Arrays.asList(rideActivities);
    }
}
