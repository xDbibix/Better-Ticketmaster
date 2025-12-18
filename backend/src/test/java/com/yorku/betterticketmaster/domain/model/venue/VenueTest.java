package com.yorku.betterticketmaster.domain.model.venue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VenueTest {

    @Test
    void gettersSettersWork() {
        Venue v = new Venue();
        v.setId("v1");
        v.setVenueName("Arena");
        v.setLocation("City");
        v.setVenueType(VenueType.ARENA);
        v.setCanvasWidth(800);
        v.setCanvasHeight(600);

        assertEquals("v1", v.getId());
        assertEquals("Arena", v.getVenueName());
        assertEquals("City", v.getLocation());
        assertEquals(VenueType.ARENA, v.getVenueType());
        assertEquals(800, v.getCanvasWidth());
        assertEquals(600, v.getCanvasHeight());
    }
}
