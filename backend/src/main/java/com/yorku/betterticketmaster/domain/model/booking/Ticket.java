package com.yorku.betterticketmaster.domain.model.booking;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection="tickets")
public class Ticket {
    @Id
    private String id;
    private String seatId;
    private String eventId;
    private String ownerId; 
    private String buyerId; // Switch only if resold
    private double purchasePrice; // Face value of ticket
    private LocalDateTime purchasedAt;
    private boolean resale = false;
    private double resalePrice;
    private String qrCode; 

    public static Ticket purchased(String eventId, String seatId, String buyerId, double purchasePrice, LocalDateTime purchasedAt) {
        Ticket t = new Ticket();
        t.eventId = eventId;
        t.seatId = seatId;
        t.buyerId = buyerId;
        t.ownerId = buyerId;
        t.purchasePrice = purchasePrice;
        t.purchasedAt = purchasedAt;
        return t;
    }

    public void listForResale(double price) {
        resale = true;
        resalePrice = price;
    }

    public void transferTo(String newOwnerId) {
        ownerId = newOwnerId;
        // Transfer is not a resale. Buyer stays as the original purchaser.
        resale = false;
        resalePrice = 0.0;
    }

    public void completeResaleTo(String newOwnerId) {
        ownerId = newOwnerId;
        buyerId = newOwnerId;
        resale = false;
        resalePrice = 0.0;
    }
}
