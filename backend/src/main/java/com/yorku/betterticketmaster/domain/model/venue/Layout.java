package com.yorku.betterticketmaster.domain.model.venue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection="layouts")
public class Layout {
    @Id
    private String id;

    private String venueId;
    private String layoutName; // eg. Ateez2023
    private String imageUrl; //Layout image
}
