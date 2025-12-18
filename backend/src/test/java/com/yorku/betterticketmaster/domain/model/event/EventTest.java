package com.yorku.betterticketmaster.domain.model.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class EventTest {

    @Test
    void validResalePriceBounds() {
        Event e = new Event("e1", "org1", "lay1", "Show", "Venue", LocalDateTime.now().plusDays(1), 10.0, 100.0, "desc", "img");
        assertTrue(e.validResalePrice(10.0));
        assertTrue(e.validResalePrice(100.0));
        assertFalse(e.validResalePrice(9.99));
        assertFalse(e.validResalePrice(100.01));
    }

    @Test
    void startedAndNotStartedChecks() {
        Event past = new Event("e2", "org1", "lay1", "Past", "Venue", LocalDateTime.now().minusHours(1), 0, 1, "d", "i");
        Event future = new Event("e3", "org1", "lay1", "Future", "Venue", LocalDateTime.now().plusHours(1), 0, 1, "d", "i");
        assertTrue(past.eventStarted());
        assertFalse(past.eventNotStarted());
        assertFalse(future.eventStarted());
        assertTrue(future.eventNotStarted());
    }
}
