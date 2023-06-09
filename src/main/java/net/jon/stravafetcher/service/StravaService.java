package net.jon.stravafetcher.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.jon.stravafetcher.model.Athlete;
import net.jon.stravafetcher.model.Comment;
import net.jon.stravafetcher.model.Follower;
import net.jon.stravafetcher.model.RideActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class StravaService {
    private static final String STRAVA_API_BASE_URL = "https://www.strava.com/api/v3";
    private static final Logger log = LoggerFactory.getLogger(StravaService.class);

    public List<RideActivity> getActivities(String accessToken, int page, int perPage, long after, long before) {
        String url = UriComponentsBuilder.fromHttpUrl(STRAVA_API_BASE_URL)
                .path("/athlete/activities")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("after", after)
                .queryParam("before", before)
                .toUriString();

        RideActivity[] rideActivities = fetchData(url, accessToken, RideActivity[].class);
        return Arrays.asList(rideActivities != null ? rideActivities : new RideActivity[0]);
    }

    public List<RideActivity> getActivities(String accessToken, int page, int perPage) {
        String url = UriComponentsBuilder.fromHttpUrl(STRAVA_API_BASE_URL)
                .path("/athlete/activities")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .toUriString();

        RideActivity[] rideActivities = fetchData(url, accessToken, RideActivity[].class);
        return Arrays.asList(rideActivities != null ? rideActivities : new RideActivity[0]);
    }

    public Athlete getAthlete(String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(STRAVA_API_BASE_URL)
                .path("/athlete")
                .toUriString();

        return fetchData(url, accessToken, Athlete.class);
    }

    public List<Comment> getActivityComments(String accessToken, long activityId) {
        String url = UriComponentsBuilder.fromHttpUrl(STRAVA_API_BASE_URL)
                .path("/activities/" + activityId + "/comments")
                .toUriString();

        Comment[] comments = fetchData(url, accessToken, Comment[].class);
        return Arrays.asList(comments != null ? comments : new Comment[0]);
    }

    public List<Follower> getActivityKudos(String accessToken, long activityId) {
        String url = UriComponentsBuilder.fromHttpUrl(STRAVA_API_BASE_URL)
                .path("/activities/" + activityId + "/kudos")
                .toUriString();

        Follower[] followers = fetchData(url, accessToken, Follower[].class);
        return Arrays.asList(followers != null ? followers : new Follower[0]);
    }

    private <T> T fetchData(String url, String accessToken, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> rawJsonResponse = null;

        try {
            rawJsonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch data from URL {}. HTTP Status Code: {}. Response Body: {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        T result = null;
        try {
            result = objectMapper.readValue(rawJsonResponse.getBody(), responseType);
        } catch (IOException e) {
            log.error("Failed to parse the fetched data: ", e);
        }

        return result;
    }
}
