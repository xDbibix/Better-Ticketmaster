package com.yorku.betterticketmaster.domain.model.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/*
Bookings dataset to track all current bookings, used for double booking protection and purchase timer


*/
@Data
@Document(collection="bookings")
public class Booking {
    @Id
    private String id;
    private String consumerId;
    private String eventId;
    private List<String> seatIds;
    private double totalPrice;
    private LocalDateTime expiry; //For timer logic, LocalDateTime.now() > expiry, release seats
    private String status; // PENDING, COMPLETED, EXPIRED
}
