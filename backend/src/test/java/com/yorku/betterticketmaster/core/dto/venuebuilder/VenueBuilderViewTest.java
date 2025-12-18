package com.yorku.betterticketmaster.core.dto.venuebuilder;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;

public class VenueBuilderViewTest {

    @Test
    void builderAndGettersWork() {
        VenueBuilderView view = VenueBuilderView.builder()
                .venueId("v1")
                .venueName("Arena")
                .layoutId("l1")
                .layoutName("Default")
                .canvasWidth(800)
                .canvasHeight(600)
                .backgroundImageUrl("img.png")
                .sections(List.of(new SectionTemplate()))
                .build();

        assertEquals("v1", view.getVenueId());
        assertEquals("Arena", view.getVenueName());
        assertEquals("l1", view.getLayoutId());
        assertEquals("Default", view.getLayoutName());
        assertEquals(800, view.getCanvasWidth());
        assertEquals(600, view.getCanvasHeight());
        assertEquals("img.png", view.getBackgroundImageUrl());
        assertEquals(1, view.getSections().size());
    }
}
