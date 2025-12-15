package com.yorku.betterticketmaster.domain.model.event;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection="seats")
public class Seat {
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

    private String status = "AVAILABLE"; // "AVAILABLE", "HELD", "SOLD" 
    private String type = "STANDARD"; // "STANDARD", "VIP", "RESALE"
    
    private double price;

    /*
    Optimistic locking, prevent double booking
    */
    @Version
    private Long version;
}
