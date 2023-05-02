package net.jon.stravafetcher.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.jon.stravafetcher.model.RideActivity;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class StravaService {
    private static final String STRAVA_API_BASE_URL = "https://www.strava.com/api/v3";

    public List<RideActivity> getActivities(String accessToken, int page, int perPage) {
        // Use UriComponentsBuilder to add query parameters
        String url = UriComponentsBuilder.fromHttpUrl(STRAVA_API_BASE_URL)
                .path("/athlete/activities")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        // Fetch the raw JSON response as a String.
        ResponseEntity<String> rawJsonResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class);

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

        return Arrays.asList(rideActivities != null ? rideActivities : new RideActivity[0]);
    }
}
