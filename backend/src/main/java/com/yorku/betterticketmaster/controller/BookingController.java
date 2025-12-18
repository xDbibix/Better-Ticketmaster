package com.yorku.betterticketmaster.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.domain.model.booking.Booking;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.services.AuthService;
import com.yorku.betterticketmaster.domain.services.BookingService;
import com.yorku.betterticketmaster.security.AuthTokenStore;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final AuthService authService;
    private final AuthTokenStore tokenStore;

    private User currentUser(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
        String userId = tokenStore.getUserId(token);
        return userId == null ? null : authService.getCurrentUser(userId).orElse(null);
    }

    public record CreateBookingRequest(String eventId, java.util.List<String> seatIds, double totalPrice) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateBookingRequest body, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        Booking b = Booking.createPending(u.getId(), body.eventId(), body.seatIds(), body.totalPrice());
        Booking created = bookingService.createBooking(b);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable String id, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        Booking b = bookingService.getBooking(id).orElse(null);
        if (b==null) return ResponseEntity.notFound().build();
        if (!b.getConsumerId().equals(u.getId())) return ResponseEntity.status(403).body("Not owner");
        Booking done = bookingService.completeBooking(id);
        return ResponseEntity.ok(done);
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<?> requestTransfer(@PathVariable String id, @RequestBody String toUserId, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        Booking b = bookingService.getBooking(id).orElse(null);
        if (b==null) return ResponseEntity.notFound().build();
        if (!b.getConsumerId().equals(u.getId())) return ResponseEntity.status(403).body("Not owner");
        Booking out = bookingService.requestTransfer(id, toUserId);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{id}/transfer/complete")
    public ResponseEntity<?> completeTransfer(@PathVariable String id, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        // allow owner or admin to complete
        Booking b = bookingService.getBooking(id).orElse(null);
        if (b==null) return ResponseEntity.notFound().build();
        if (!b.getConsumerId().equals(u.getId()) && !u.isAdmin()) return ResponseEntity.status(403).body("Not allowed");
        Booking out = bookingService.completeTransfer(id);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    public ResponseEntity<?> listMine(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        List<Booking> list = bookingService.listByUser(u.getId());
        return ResponseEntity.ok(list);
    }
}
