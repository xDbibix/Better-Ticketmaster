package com.yorku.betterticketmaster.domain.model.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SeatTest {

    @Test
    void statusTransitions() {
        Seat s = new Seat("id", "e1", "A", "1", 1, 20.0);
        assertTrue(s.isAvailable());

        s.holdSeat();
        assertFalse(s.isAvailable());
        assertThrows(IllegalStateException.class, s::holdSeat);

        s.sellSeat();
        assertThrows(IllegalStateException.class, s::sellSeat);
        assertThrows(IllegalStateException.class, s::releaseSeat);
    }

    @Test
    void releaseFromHeld() {
        Seat s = new Seat("id", "e1", "A", "1", 1, 20.0);
        s.holdSeat();
        s.releaseSeat();
        assertTrue(s.isAvailable());
    }
}
