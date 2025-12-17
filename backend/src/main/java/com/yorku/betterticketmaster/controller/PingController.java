package com.yorku.betterticketmaster.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Test controller to verify that the backend is running and Spring Boot is set up correctly bc its stupid
@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
