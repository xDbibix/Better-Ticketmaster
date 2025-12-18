package com.yorku.betterticketmaster.domain.model.event;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * Event entity with scheduling, resale constraints, and status helpers.
 */
@Data
@Document(collection="events")
public class Event {
    @Id
    private String id;
    private String organizerId;
    private String layoutId;
    private String title;
    private String venueName;
    private String status; 
    private LocalDateTime dateTime;

    //Resale constraints
    private double minResale;
    private double maxResale;

    //Frontend

    private String description; 
    //event banner
    private String imageUrl;

    public Event() {}

    public Event(String id, String organizerId, String layoutId, String title, String venueName, LocalDateTime dateTime, double minResale, double maxResale, String description, String imageUrl) {
        this.id = id;
        this.organizerId = organizerId;
        this.layoutId = layoutId;
        this.title = title;
        this.venueName = venueName;
        this.dateTime = dateTime;
        this.minResale = minResale;
        this.maxResale = maxResale;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = "APPROVED";
    }

    public boolean validResalePrice(double price) {
        return price <= maxResale && price >= minResale;
    }

    public boolean eventStarted() {
        return LocalDateTime.now().isAfter(dateTime);
    }

    public boolean eventNotStarted() {
        return LocalDateTime.now().isBefore(dateTime);
    }

    public boolean isClosed() {
        return status != null && "CLOSED".equalsIgnoreCase(status.trim());
    }
}
