package com.yorku.betterticketmaster.domain.model.booking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class TicketTest {

    @Test
    void ticketOwnerResaleSetsFields() {
        Ticket t = new Ticket("t1", "s1", "e1", "owner", "buyer", 30.0, LocalDateTime.now(), false, 0.0, "qr");
        assertFalse(t.isResale());
        t.ticketOwnerResale("newBuyer", 45.0);
        assertTrue(t.isResale());
        assertEquals("newBuyer", t.getBuyerId());
        assertEquals(45.0, t.getResalePrice());
    }
}
