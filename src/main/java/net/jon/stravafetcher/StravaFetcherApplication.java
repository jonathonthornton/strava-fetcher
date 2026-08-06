package net.jon.stravafetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StravaFetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(StravaFetcherApplication.class, args);
    }

}
