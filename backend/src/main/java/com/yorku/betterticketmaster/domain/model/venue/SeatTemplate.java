package com.yorku.betterticketmaster.domain.model.venue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/**
 * Seating chart for venue builder
 * Specific to each layout
 */
@Data
@Document(collection="seat_templates")
public class SeatTemplate {
    @Id
    private String id; //MongoDB generates this, per seat id

    private String layoutId; 
    private String section;
    private char row; //A-Z, AA-ZZ
    private int seatNum;
    /*
    Coordinates for venue builder & frontend. Map seat to specific pixel on map, render circles at x and y
    Someone tell felix this 
    */
    private int x;
    private int y;
}
