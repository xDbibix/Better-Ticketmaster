package com.yorku.betterticketmaster.core.dto.venuebuilder;

import java.util.List;

import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueBuilderView {
    private String venueId;
    private String venueName;
    private String layoutId;
    private String layoutName;

    private int canvasWidth;
    private int canvasHeight;

    private String backgroundImageUrl;

    private List<SectionTemplate> sections;
}
