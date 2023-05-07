package net.jon.stravafetcher;

import net.jon.stravafetcher.client.StravaClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StravaFetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(StravaFetcherApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(StravaClient stravaClient) {
        return (args) -> stravaClient.fetchActivities();
    }

}
