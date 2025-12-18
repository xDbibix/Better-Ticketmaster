package com.yorku.betterticketmaster.domain.services;

import com.yorku.betterticketmaster.domain.model.booking.Ticket;
import com.yorku.betterticketmaster.domain.model.event.Event;
import com.yorku.betterticketmaster.domain.model.users.Role;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.repository.booking.TicketRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepo;
    @Mock LayoutRepository layoutRepo;
    @Mock EventRepository eventRepo;
    @Mock TicketRepository ticketRepo;

    @InjectMocks UserService service;

    @Test
    void signUp_duplicateEmail_throws() {
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(new User("id","a@b.com","p","n", Role.CONSUMER)));
        assertThrows(IllegalStateException.class, () -> service.signUp("a@b.com","p","n", Role.CONSUMER));
    }

    @Test
    void signUp_success_saves() {
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        User created = service.signUp("a@b.com","pass","Alice", Role.CONSUMER);
        assertEquals("a@b.com", created.getEmail());
        assertEquals("Alice", created.getName());
    }

    @Test
    void login_emailNotFound_throws() {
        when(userRepo.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.login("x@y.com","p"));
    }

    @Test
    void login_wrongPassword_throws() {
        User u = new User("id","a@b.com","right","n", Role.CONSUMER);
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        assertThrows(IllegalArgumentException.class, () -> service.login("a@b.com","wrong"));
    }

    @Test
    void login_success_returnsUser() {
        User u = new User("id","a@b.com","pw","n", Role.CONSUMER);
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        User result = service.login("a@b.com","pw");
        assertEquals("a@b.com", result.getEmail());
    }

    @Test
    void createLayout_requiresAdmin() {
        User nonAdmin = new User("id","e","p","n", Role.CONSUMER);
        assertThrows(IllegalStateException.class, () -> service.createLayout(nonAdmin, new Layout()));
        verify(layoutRepo, never()).save(any());
    }

    @Test
    void createLayout_admin_saves() {
        User admin = new User("id","e","p","n", Role.ADMIN);
        Layout l = new Layout();
        when(layoutRepo.save(any(Layout.class))).thenAnswer(inv -> inv.getArgument(0));
        Layout saved = service.createLayout(admin, l);
        assertNotNull(saved);
        verify(layoutRepo).save(l);
    }

    @Test
    void createEvent_requiresOrganizer() {
        User cons = new User("id","e","p","n", Role.CONSUMER);
        assertThrows(IllegalStateException.class, () -> service.createEvent(cons, new Event("i","o","l","t","v", LocalDateTime.now(),0,1,"d","i")));
        verify(eventRepo, never()).save(any());
    }

    @Test
    void createEvent_organizer_saves() {
        User org = new User("id","e","p","n", Role.ORGANIZER);
        Event e = new Event("i","o","l","t","v", LocalDateTime.now(),0,1,"d","i");
        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        Event saved = service.createEvent(org, e);
        assertNotNull(saved);
        verify(eventRepo).save(e);
    }

    @Test
    void ticketTransfer_consumerAndOwner_succeeds() {
        User cons = new User("u1","e","p","n", Role.CONSUMER);
        Ticket t = new Ticket("t1","s1","e1","owner","u1", 10.0, LocalDateTime.now(), false, 0.0, "qr");
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        Ticket saved = service.ticketTransfer(cons, t, "u2");
        assertEquals("u2", saved.getBuyerId());
        verify(ticketRepo).save(t);
    }

    @Test
    void ticketTransfer_notOwner_throws() {
        User cons = new User("u1","e","p","n", Role.CONSUMER);
        Ticket t = new Ticket("t1","s1","e1","owner","other", 10.0, LocalDateTime.now(), false, 0.0, "qr");
        assertThrows(IllegalStateException.class, () -> service.ticketTransfer(cons, t, "u2"));
        verify(ticketRepo, never()).save(any());
    }

    @Test
    void resellTicket_setsFields() {
        User cons = new User("u1","e","p","n", Role.CONSUMER);
        Ticket t = new Ticket("t1","s1","e1","owner","u1", 10.0, LocalDateTime.now(), false, 0.0, "qr");
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        Ticket saved = service.resellTicket(cons, t, 25.0);
        assertTrue(saved.isResale());
        assertEquals(25.0, saved.getResalePrice());
        verify(ticketRepo).save(t);
    }

    @Test
    void getUserById_found_returns() {
        User u = new User("id","e","p","n", Role.CONSUMER);
        when(userRepo.findById("id")).thenReturn(Optional.of(u));
        assertEquals(u, service.getUserById("id"));
    }

    @Test
    void getUserById_missing_throws() {
        when(userRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getUserById("missing"));
    }
}
