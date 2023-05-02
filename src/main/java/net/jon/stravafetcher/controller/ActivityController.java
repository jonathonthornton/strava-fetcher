package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.service.EddingtonNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
public class ActivityController {
    @Autowired
    private EddingtonNumberService eddingtonNumberService;

    @GetMapping("/eddington-number")
    public int getEddingtonNumber() {
        return eddingtonNumberService.calculateEddingtonNumber();
    }
}
