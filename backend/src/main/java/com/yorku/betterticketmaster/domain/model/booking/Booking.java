package com.yorku.betterticketmaster.domain.model.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/*
Bookings dataset to track all current bookings, used for double booking 
protection and purchase timer
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
    private String status; // PENDING, COMPLETED, EXPIRED. CANCELLED

    //constructor
    public Booking(String id, String consumerId, String eventId, List<String> seatIds, double totalPrice) {
        this.id = id;
        this.consumerId = consumerId;
        this.eventId = eventId;
        this.seatIds = seatIds;
        this.totalPrice = totalPrice;
        this.expiry = LocalDateTime.now().plusMinutes(5); //5 minutes until booking expires
        this.status = "PENDING";
    }   

    public void bookingComplete() {
        if (!("PENDING".equals(this.status))) {
            throw new IllegalStateException("Bookings are only available to those with a pending status.");
        }
        this.status = "COMPLETED";
        this.expiry = null;
    }

    public void bookingCancel() {
        if ("COMPLETED".equals(this.status)) {
            throw new IllegalStateException("Completed bookings cannnot be cancelled. All sales are final.");
        }
        this.status = "CANCELLED";
    }

    public void bookingExpire() {
        if ("PENDING".equals(this.status) && this.expiry != null && LocalDateTime.now().isAfter(this.expiry)) {
            this.status = "EXPIRED";
        }
    }
}
