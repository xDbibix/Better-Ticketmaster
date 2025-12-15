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
    private String purchasePrice; //Face Value of ticket
    private LocalDateTime purchasedAt;
    private boolean resale = false;
    private double resalePrice;
    private String qrCode; 

}
