package com.yorku.betterticketmaster.domain.model.booking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

public class BookingTest {

    @Test
    void initialAndCompleteFlow() {
        Booking b = new Booking("b1", "c1", "e1", List.of("s1", "s2"), 50.0);
        assertEquals("PENDING", b.getStatus());
        assertNotNull(b.getExpiry());

        b.bookingComplete();
        assertEquals("COMPLETED", b.getStatus());
        assertNull(b.getExpiry());

        // Completed bookings cannot be cancelled
        assertThrows(IllegalStateException.class, b::bookingCancel);
    }

    @Test
    void cancelFromPending() {
        Booking b = new Booking("b2", "c1", "e1", List.of(), 0.0);
        b.bookingCancel();
        assertEquals("CANCELLED", b.getStatus());
    }

    @Test
    void expireWhenPastExpiry() {
        Booking b = new Booking("b3", "c1", "e1", List.of(), 0.0);
        b.setExpiry(LocalDateTime.now().minusMinutes(1));
        b.bookingExpire();
        assertEquals("EXPIRED", b.getStatus());
    }
}
