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

    private String eventId;
    private String section;
    private String row; //A-Z, AA-ZZ
    private Integer seatNum; //Null if GA
    /*
    Coordinates for venue builder & frontend. Map seat to specific pixel on map, render circles at x and y
    Someone tell felix this 
    */
    private Double x;
    private Double y;

    private String status = "AVAILABLE"; // "AVAILABLE", "HELD", "SOLD" 
    private String type = "STANDARD"; // "STANDARD", "VIP", "RESALE"
    
    private double price;

    /*
    Optimistic locking, prevent double booking
    */
    @Version
    private Long version;

    public Seat(String id, String eventId, String section, String row, int seatNum, double price){
        this.id = id;
        this.eventId = eventId;
        this.section = section;
        this.row = row;
        this.seatNum = seatNum;
        this.price = price;
    }

    public void holdSeat(){
        if(!("AVAILABLE".equals(this.status))){
            throw new IllegalStateException("Only available seats can be held.");
        }
        this.status = "HELD";
    }

    public void sellSeat(){
        if(!("HELD".equals(this.status))){
            throw new IllegalStateException("Only held seats can be sold.");
        }
        this.status = "SOLD";
    }

    public void releaseSeat(){
        if(!("HELD".equals(this.status))){
            throw new IllegalStateException("Only held seats can be released.");
        }
        this.status = "AVAILABLE";
    }

    public boolean isAvailable(){
        return "AVAILABLE".equals(this.status);
    }
}
