package com.yorku.betterticketmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.yorku.betterticketmaster")
public class BetterticketmasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetterticketmasterApplication.class, args);
    }
}
