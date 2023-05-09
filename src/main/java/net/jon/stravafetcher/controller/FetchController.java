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
    public void fetchActivities(@PathVariable String accessToken) {
        fetchService.fetchAthlete(accessToken);
    }

    @GetMapping("/all/{accessToken}/{afterYear}/{afterMonth}/{beforeYear}/{beforeMonth}")
    public void fetchAll(@PathVariable String accessToken, @PathVariable int afterYear, @PathVariable int afterMonth, @PathVariable int beforeYear, @PathVariable int beforeMonth) {
        fetchService.fetchAll(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
    }

    @GetMapping("/activities/{accessToken}/{afterYear}/{afterMonth}/{beforeYear}/{beforeMonth}")
    public void fetchActivities(@PathVariable String accessToken, @PathVariable int afterYear, @PathVariable int afterMonth, @PathVariable int beforeYear, @PathVariable int beforeMonth) {
        fetchService.fetchActivities(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
    }

    @GetMapping("/kudos/{accessToken}/{afterYear}/{afterMonth}/{beforeYear}/{beforeMonth}")
    public void fetchKudos(@PathVariable String accessToken, @PathVariable int afterYear, @PathVariable int afterMonth, @PathVariable int beforeYear, @PathVariable int beforeMonth) {
        fetchService.fetchKudos(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
    }

    @GetMapping("/comments/{accessToken}/{afterYear}/{afterMonth}/{beforeYear}/{beforeMonth}")
    public void fetchComments(@PathVariable String accessToken, @PathVariable int afterYear, @PathVariable int afterMonth, @PathVariable int beforeYear, @PathVariable int beforeMonth) {
        fetchService.fetchComments(accessToken, afterYear, afterMonth, beforeYear, beforeMonth);
    }
}
