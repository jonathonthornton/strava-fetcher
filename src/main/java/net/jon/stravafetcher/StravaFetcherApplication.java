package net.jon.stravafetcher;

import net.jon.stravafetcher.client.StravaClient;
import net.jon.stravafetcher.service.EddingtonNumberService;
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
        return (args) -> stravaClient.authorizeAndFetchActivities();
    }

//    @Bean
//    public CommandLineRunner run(EddingtonNumberService eddingtonNumber) {
//        return (args) -> System.out.println("Eddington number: " + eddingtonNumber.calculateEddingtonNumber());
//    }
}
