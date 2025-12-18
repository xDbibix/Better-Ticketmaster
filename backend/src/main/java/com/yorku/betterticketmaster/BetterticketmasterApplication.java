package com.yorku.betterticketmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for Better Ticketmaster.
 */
@SpringBootApplication(scanBasePackages = "com.yorku.betterticketmaster")
public class BetterticketmasterApplication {

    /**
     * Bootstraps the application.
     * @param args CLI args
     */
    public static void main(String[] args) {
        SpringApplication.run(BetterticketmasterApplication.class, args);
    }
}
