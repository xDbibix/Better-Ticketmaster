package com.yorku.betterticketmaster.domain.model.venue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/**
 * Venue Section Layout
 * Specific to each venue
 */
@Data
@Document(collection="layouts")
public class Layout {
    @Id
    private String id;

    private String venueId;
    private String layoutName; // eg. Ateez2023
    private String imageUrl; //Layout image
/* 
    public Layout(String id, String venueId, String layoutName, String imageUrl){
        this.id = id;
        this.venueId = venueId;
        this.layoutName = layoutName;
        this.imageUrl = imageUrl;
    }*/
}