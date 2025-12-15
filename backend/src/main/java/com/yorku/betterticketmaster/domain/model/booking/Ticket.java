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
    private String buyerId; //Switch only if resold
    private double purchasePrice; //Face Value of ticket -> changed from string to double
    private LocalDateTime purchasedAt;
    private boolean resale = false;
    private double resalePrice;
    private String qrCode; 

    public Ticket(String id, String seatId, String eventId, String ownerId, String buyerId, double purchasePrice, LocalDateTime purchasedAt, boolean resale, double resalePrice, String qrCode) {
        this.id = id;
        this.seatId = seatId;
        this.eventId = eventId;
        this.ownerId = ownerId;
        this.buyerId = buyerId;
        this.purchasePrice = purchasePrice;
        this.purchasedAt = purchasedAt;
        this.resale = resale;
        this.resalePrice = resalePrice;
        this.qrCode = qrCode;
    }

    public void ticketOwnerResale(String resaleOwner, double price) {
        this.buyerId = resaleOwner;
        this.resalePrice = price;
        this.resale = true;
    }

    public boolean isResale() {
        return resale;
    }

}
