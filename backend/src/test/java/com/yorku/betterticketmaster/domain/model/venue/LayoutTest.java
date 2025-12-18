package com.yorku.betterticketmaster.domain.model.venue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutTest {

    @Test
    void gettersSettersWork() {
        Layout l = new Layout();
        l.setId("l1");
        l.setVenueId("v1");
        l.setLayoutName("Default");
        l.setImageUrl("img.png");

        assertEquals("l1", l.getId());
        assertEquals("v1", l.getVenueId());
        assertEquals("Default", l.getLayoutName());
        assertEquals("img.png", l.getImageUrl());
    }
}
