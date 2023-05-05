package net.jon.stravafetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StravaFetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(StravaFetcherApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner run(StravaClient stravaClient) {
//        return (args) -> stravaClient.fetchActivities();
//    }
}
