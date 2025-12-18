package com.yorku.betterticketmaster.domain.model.event;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * Seat entity with optimistic locking and lifecycle controls (hold, sell, release).
 */
@Data
@Document(collection = "seats")
public class Seat {
    @Id
    private String id; //MongoDB generates this, per seat id

    private String eventId;
    private String section;
    private String row; //A-Z, AA-ZZ
    private int seatNum;
    private double price;

    private String status = "AVAILABLE";

    private Instant holdUntil;

    @Version
    private Long version;

    public Seat() {}

    public Seat(String id, String eventId, String section, String row, int seatNum, double price) {
        this.id = id;
        this.eventId = eventId;
        this.section = section;
        this.row = row;
        this.seatNum = seatNum;
        this.price = price;
    }

    public void holdSeat(Instant until) {
        if (!"AVAILABLE".equals(this.status)) {
            throw new IllegalStateException("Only available seats can be held.");
        }
        this.status = "HELD";
        this.holdUntil = until;
    }

    public void sellSeat() {
        if (!"HELD".equals(this.status)) {
            throw new IllegalStateException("Only held seats can be sold.");
        }
        this.status = "SOLD";
        this.holdUntil = null;
    }

    public void releaseSeat() {
        if (!"HELD".equals(this.status)) {
            throw new IllegalStateException("Only held seats can be released.");
        }
        this.status = "AVAILABLE";
        this.holdUntil = null;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(this.status);
    }
}
