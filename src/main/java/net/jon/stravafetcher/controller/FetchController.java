package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.service.FetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fetch")
public class FetchController {
    private static final Logger log = LoggerFactory.getLogger(FetchController.class);

    @Autowired
    private FetchService fetchService;

    @GetMapping("/activities/{accessToken}")
    public void fetchActivities(@PathVariable String accessToken) {
//        fetchService.fetchAthlete(accessToken);
//        fetchService.fetchActivities(accessToken);
//        fetchService.fetchComments(accessToken);
    }
}
