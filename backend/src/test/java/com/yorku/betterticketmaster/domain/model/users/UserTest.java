package com.yorku.betterticketmaster.domain.model.users;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void passwordEncodingAndVerify() {
        User u = new User("id1", "user@example.com", "secret", "Alice", Role.CONSUMER);
        assertTrue(u.verifyPassword("secret"));
        assertFalse(u.verifyPassword("wrong"));
    }

    @Test
    void roleChecks() {
        User admin = new User("id2", "a@b.com", "p", "Admin", Role.ADMIN);
        assertTrue(admin.isAdmin());
        assertFalse(admin.isOrganizer());
        assertFalse(admin.isConsumer());

        User org = new User("id3", "o@b.com", "p", "Org", Role.ORGANIZER);
        assertTrue(org.isOrganizer());
        assertFalse(org.isAdmin());
        assertFalse(org.isConsumer());

        User cons = new User("id4", "c@b.com", "p", "Cons", Role.CONSUMER);
        assertTrue(cons.isConsumer());
        assertFalse(cons.isAdmin());
        assertFalse(cons.isOrganizer());
    }

    @Test
    void ownedTicketOperations() {
        User u = new User("id5", "user2@example.com", "pw", "Bob", Role.CONSUMER);
        assertFalse(u.ownsTicket("T1"));
        u.addOwnedTicket("T1");
        assertTrue(u.ownsTicket("T1"));
        // adding duplicate should not break
        u.addOwnedTicket("T1");
        assertTrue(u.ownsTicket("T1"));
        u.removeOwnedTicket("T1");
        assertFalse(u.ownsTicket("T1"));
    }
}
