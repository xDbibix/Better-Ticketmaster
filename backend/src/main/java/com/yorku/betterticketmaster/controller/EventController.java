package com.yorku.betterticketmaster.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.domain.model.event.Event;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.services.AuthService;
import com.yorku.betterticketmaster.domain.services.EventService;
import com.yorku.betterticketmaster.security.AuthTokenStore;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final AuthService authService;
    private final AuthTokenStore tokenStore;

    private User currentUser(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
        String userId = tokenStore.getUserId(token);
        return userId == null ? null : authService.getCurrentUser(userId).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(name = "q", required = false) String q,
        @RequestParam(name = "mine", required = false) Boolean mine,
        HttpServletRequest req
    ) {
        String query = (q == null ? "" : q).trim().toLowerCase();
        boolean onlyMine = Boolean.TRUE.equals(mine);

        List<Event> all = eventService.search(query);
        java.util.ArrayList<Event> out = new java.util.ArrayList<>();

        if (onlyMine) {
            User u = currentUser(req);
            if (u == null) return ResponseEntity.status(401).body("Login required");
            if (!(u.isOrganizer() || u.isAdmin())) return ResponseEntity.status(403).body("Organizer/Admin required");

            for (Event ev : all) {
                if (!u.isAdmin()) {
                    if (ev.getOrganizerId() == null || !ev.getOrganizerId().equals(u.getId())) continue;
                }
                if (!query.isEmpty()) {
                    String hay = ((ev.getTitle() == null ? "" : ev.getTitle()) + " " + (ev.getVenueName() == null ? "" : ev.getVenueName()))
                        .toLowerCase();
                    if (!hay.contains(query)) continue;
                }
                out.add(ev);
            }
            return ResponseEntity.ok(out);
        }

        // Public list: hide CLOSED and REJECTED. (For demo usability, allow PENDING + APPROVED.)
        for (Event ev : all) {
            if (ev == null) continue;
            if (ev.isClosed()) continue;
            if (ev.getStatus() != null && "REJECTED".equalsIgnoreCase(ev.getStatus().trim())) continue;
            if (!query.isEmpty()) {
                String hay = ((ev.getTitle() == null ? "" : ev.getTitle()) + " " + (ev.getVenueName() == null ? "" : ev.getVenueName()))
                    .toLowerCase();
                if (!hay.contains(query)) continue;
            }
            out.add(ev);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Optional<Event> e = eventService.getEvent(id);
        return e.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Event e, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");
        // Organizers may request creation; admins may create directly
        if (u.isOrganizer()) {
            Event created = eventService.requestEventCreation(e, u.getId());
            return ResponseEntity.ok(created);
        }
        if (u.isAdmin()) {
            Event created = eventService.createEvent(e);
            return ResponseEntity.ok(created);
        }
        return ResponseEntity.status(403).body("Insufficient role");
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable String id, HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).body("Login required");

        Event ev = eventService.getEvent(id).orElse(null);
        if (ev == null) return ResponseEntity.notFound().build();

        if (u.isAdmin()) {
            return ResponseEntity.ok(eventService.closeEvent(id));
        }

        if (u.isOrganizer()) {
            if (ev.getOrganizerId() == null || !ev.getOrganizerId().equals(u.getId())) {
                return ResponseEntity.status(403).body("Not your event");
            }
            return ResponseEntity.ok(eventService.closeEvent(id));
        }

        return ResponseEntity.status(403).body("Insufficient role");
    }
}
