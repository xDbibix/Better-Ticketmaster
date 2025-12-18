package com.yorku.betterticketmaster.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.domain.model.booking.Ticket;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.repository.booking.TicketRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;
import com.yorku.betterticketmaster.domain.services.AuthService;
import com.yorku.betterticketmaster.domain.services.BookingService;
import com.yorku.betterticketmaster.domain.services.UserService;
import com.yorku.betterticketmaster.security.AuthTokenStore;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Ticket endpoints for resale listings, purchase, transfer, and user tickets.
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final BookingService bookingService;
    private final AuthService authService;
    private final AuthTokenStore tokenStore;
    private final TicketRepository ticketRepo;
    private final UserService userService;
    private final EventRepository eventRepo;

    private User currentUser(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
        String userId = tokenStore.getUserId(token);
        return userId == null ? null : authService.getCurrentUser(userId).orElse(null);
    }

    @GetMapping("/resale")
    /**
     * List resale tickets for an event.
     * @param eventId event identifier
     * @return resale tickets
     */
    public ResponseEntity<?> listResale(@RequestParam String eventId) {
        List<Ticket> list = bookingService.listResaleTickets(eventId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/mine")
    /**
     * List tickets owned by the current user.
     * @param req HTTP request
     * @return tickets for user or error
     */
    public ResponseEntity<?> listMine(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        return ResponseEntity.ok(ticketRepo.findByOwnerId(u.getId()));
    }

    public record ResaleBody(double price) {}

    @PostMapping("/{id}/resell")
    /**
     * List a ticket for resale.
     * @param id ticket id
     * @param body resale payload
     * @param req HTTP request
     * @return updated ticket or error
     */
    public ResponseEntity<?> resell(@PathVariable String id, @RequestBody ResaleBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        try {
            Ticket t = bookingService.resellTicket(id, body.price, u.getId());
            return ResponseEntity.ok(t);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/buy")
    /**
     * Purchase a resale ticket.
     * @param id ticket id
     * @param req HTTP request
     * @return purchased ticket or error
     */
    public ResponseEntity<?> buy(@PathVariable String id, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        try {
            Ticket t = bookingService.purchaseResale(id, u.getId());
            return ResponseEntity.ok(t);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    public record TransferBody(String toEmail) {}

    @PostMapping("/{id}/transfer")
    /**
     * Transfer a ticket to another user.
     * @param id ticket id
     * @param body transfer payload
     * @param req HTTP request
     * @return updated ticket or error
     */
    public ResponseEntity<?> transfer(@PathVariable String id, @RequestBody TransferBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        if (body == null || body.toEmail() == null || body.toEmail().isBlank()) {
            return ResponseEntity.badRequest().body("toEmail is required");
        }

        Ticket t = ticketRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        if (!u.getId().equals(t.getOwnerId())) return ResponseEntity.status(403).body("Not owner");
        if (t.isResale()) return ResponseEntity.status(400).body("Ticket is listed for resale");

        var ev = eventRepo.findById(t.getEventId()).orElse(null);
        if (ev != null && !ev.eventNotStarted()) {
            return ResponseEntity.status(400).body("Cannot transfer after event start");
        }

        var recipient = userService.findByEmail(body.toEmail().trim()).orElse(null);
        if (recipient == null) return ResponseEntity.status(400).body("Recipient not found");
        if (recipient.getId().equals(u.getId())) return ResponseEntity.status(400).body("Cannot transfer to yourself");

        t.transferTo(recipient.getId());
        ticketRepo.save(t);
        return ResponseEntity.ok(t);
    }
}
