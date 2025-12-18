package com.yorku.betterticketmaster.domain.model.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/*
Bookings dataset to track all current bookings, used for double booking protection and purchase timers.
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
    private String status; 

    public static Booking createPending(String consumerId, String eventId, List<String> seatIds, double totalPrice) {
        Booking b = new Booking();
        b.consumerId = consumerId;
        b.eventId = eventId;
        b.seatIds = seatIds;
        b.totalPrice = totalPrice;
        b.status = "PENDING";
        b.expiry = LocalDateTime.now().plusMinutes(10);
        return b;
    }

    public void ensurePendingInitialized() {
        if (status == null || status.isBlank()) status = "PENDING";
        if (expiry == null) expiry = LocalDateTime.now().plusMinutes(10);
    }

    public void markCompleted() {
        status = "COMPLETED";
    }

    public void markExpired() {
        status = "EXPIRED";
    }

    public void requestTransfer() {
        status = "TRANSFER_REQUESTED";
    }
}
