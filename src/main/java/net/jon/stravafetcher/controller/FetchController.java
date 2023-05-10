package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.service.FetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fetch")
public class FetchController {
    private static final Logger log = LoggerFactory.getLogger(FetchController.class);

    private final FetchService fetchService;

    public FetchController(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    @GetMapping("/athlete/{accessToken}")
    public void fetchAthlete(@PathVariable String accessToken) {
        fetchService.fetchAthlete(accessToken);
    }

    @GetMapping("/activities/recent/{accessToken}")
    public void fetchRecentActivities(@PathVariable String accessToken) {
        fetchService.fetchRecentActivities(accessToken);
    }
    @GetMapping("/activities/older/{accessToken}")
    public void fetchOlderActivities(@PathVariable String accessToken) {
        fetchService.fetchOlderActivities(accessToken);
    }

    @GetMapping("/kudos/{accessToken}")
    public void fetchKudos(@PathVariable String accessToken) {
        fetchService.fetchKudos(accessToken);
    }

    @GetMapping("/comments/{accessToken}")
    public void fetchComments(@PathVariable String accessToken) {
        fetchService.fetchComments(accessToken);
    }
}
