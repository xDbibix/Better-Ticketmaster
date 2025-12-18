package com.yorku.betterticketmaster.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.domain.model.event.Event;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.repository.booking.BookingRepository;
import com.yorku.betterticketmaster.domain.repository.booking.TicketRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.SeatRepository;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.services.AuthService;
import com.yorku.betterticketmaster.domain.services.EventService;
import com.yorku.betterticketmaster.domain.services.venuebuilder.VenueBuilderService;
import com.yorku.betterticketmaster.security.AuthTokenStore;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;
    private final VenueBuilderService venueBuilderService;
    private final AuthTokenStore tokenStore;
    private final EventService eventService;
    private final TicketRepository ticketRepo;
    private final BookingRepository bookingRepo;
    private final SeatRepository seatRepo;
    private final UserRepository userRepo;

    private static final DateTimeFormatter LOCAL_DT_NO_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static LocalDateTime parseLocalDateTime(String value) {
        if (value == null) return null;
        String s = value.trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(s, LOCAL_DT_NO_SECONDS);
        }
    }

    private User currentUser(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
        String userId = tokenStore.getUserId(token);
        return userId == null ? null : authService.getCurrentUser(userId).orElse(null);
    }

    public record CreateLayoutRequest(String venueId, String layoutName, String imageUrl) {}

    @PostMapping("/layouts")
    public ResponseEntity<?> createLayout(@RequestBody CreateLayoutRequest l, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");
        Layout created = venueBuilderService.createLayout(l.venueId(), l.layoutName(), l.imageUrl());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/events/pending")
    public ResponseEntity<?> listPendingEvents(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");
        return ResponseEntity.ok(eventService.listPendingEvents());
    }

    @PostMapping("/events/{id}/approve")
    public ResponseEntity<?> approveEvent(@PathVariable String id, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");
        return ResponseEntity.ok(eventService.approveEvent(id));
    }

    @PostMapping("/events/{id}/reject")
    public ResponseEntity<?> rejectEvent(@PathVariable String id, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");
        return ResponseEntity.ok(eventService.rejectEvent(id));
    }

    @GetMapping("/events")
    public ResponseEntity<?> listAllEvents(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");
        return ResponseEntity.ok(eventService.search(""));
    }

    public record UpdateEventRequest(
        String title,
        String venueName,
        String dateTime,
        Double minResale,
        Double maxResale,
        String description,
        String imageUrl
    ) {}

    @PostMapping("/events/{id}/update")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody UpdateEventRequest body, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");
        if (body == null) return ResponseEntity.badRequest().body("Body required");

        Event ev = eventService.getEvent(id).orElse(null);
        if (ev == null) return ResponseEntity.notFound().build();

        if (body.title() != null && !body.title().trim().isEmpty()) ev.setTitle(body.title().trim());
        if (body.venueName() != null && !body.venueName().trim().isEmpty()) ev.setVenueName(body.venueName().trim());
        if (body.description() != null) ev.setDescription(body.description());
        if (body.imageUrl() != null) ev.setImageUrl(body.imageUrl());

        if (body.dateTime() != null && !body.dateTime().trim().isEmpty()) {
            try {
                LocalDateTime dt = parseLocalDateTime(body.dateTime());
                if (dt == null) return ResponseEntity.badRequest().body("Invalid dateTime");
                ev.setDateTime(dt);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid dateTime");
            }
        }

        Double min = body.minResale();
        Double max = body.maxResale();
        if (min != null || max != null) {
            double newMin = min != null ? min.doubleValue() : ev.getMinResale();
            double newMax = max != null ? max.doubleValue() : ev.getMaxResale();
            if (Double.isNaN(newMin) || Double.isNaN(newMax) || newMin < 0 || newMax < newMin) {
                return ResponseEntity.badRequest().body("Resale range invalid (min <= max)");
            }
            ev.setMinResale(newMin);
            ev.setMaxResale(newMax);
        }

        return ResponseEntity.ok(eventService.updateEvent(ev));
    }

    /**
     * Reset ONLY resale listings across ALL users (keeps ticket ownership intact).
     */
    @PostMapping("/reset/resales")
    public ResponseEntity<?> resetAllResales(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");

        int cleared = 0;
        for (var t : ticketRepo.findAll()) {
            if (t != null && t.isResale()) {
                t.setResale(false);
                t.setResalePrice(0.0);
                ticketRepo.save(t);
                cleared++;
            }
        }
        return ResponseEntity.ok(Map.of("resalesCleared", cleared));
    }

    /**
     * Reset ALL tickets and resale listings across ALL users.
     * Also clears bookings and resets all seats to AVAILABLE (clearing holds and sold status).
     *
     */
    @PostMapping("/reset/tickets")
    public ResponseEntity<?> resetAllTickets(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) return ResponseEntity.status(403).body("Admin required");

        long ticketCount = ticketRepo.count();
        long bookingCount = bookingRepo.count();

        ticketRepo.deleteAll();

        bookingRepo.deleteAll();

        // Clear ownership lists.
        int usersUpdated = 0;
        for (User user : userRepo.findAll()) {
            if (user.getOwnedTicketIds() != null && !user.getOwnedTicketIds().isEmpty()) {
                user.setOwnedTicketIds(new ArrayList<>());
                userRepo.save(user);
                usersUpdated++;
            }
        }

        // Reset all seats so they can be purchased again.
        int seatsReset = 0;
        for (var seat : seatRepo.findAll()) {
            boolean needsReset = (!"AVAILABLE".equals(seat.getStatus())) || (seat.getHoldUntil() != null);
            if (needsReset) {
                seat.setStatus("AVAILABLE");
                seat.setHoldUntil(null);
                seatRepo.save(seat);
                seatsReset++;
            }
        }

        return ResponseEntity.ok(Map.of(
            "deletedTickets", ticketCount,
            "deletedBookings", bookingCount,
            "usersCleared", usersUpdated,
            "seatsReset", seatsReset
        ));
    }
}
