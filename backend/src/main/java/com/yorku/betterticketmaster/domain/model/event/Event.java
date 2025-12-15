package com.yorku.betterticketmaster.domain.model.event;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection="events")
public class Event {
    @Id
    private String id;
    private String organizerId;
    private String layoutId;
    private String title;
    private String venueName;
    private LocalDateTime dateTime;

    //Resale constraints
    private double minResale;
    private double maxResale;

    //Frontend

    private String description; 
    //event banner
    private String imageUrl;
}
