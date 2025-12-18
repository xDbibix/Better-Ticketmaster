package com.yorku.betterticketmaster.domain.model.venue;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Venue {
    @Id
    private String id;

    private String venueName;
    private String location; 
    private VenueType venueType;
    private int canvasWidth = 1200;
    private int canvasHeight = 800;
}
