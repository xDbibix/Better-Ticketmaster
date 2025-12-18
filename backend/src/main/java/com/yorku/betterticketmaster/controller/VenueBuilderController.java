package com.yorku.betterticketmaster.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.VenueRepository;
import com.yorku.betterticketmaster.domain.services.AuthService;
import com.yorku.betterticketmaster.domain.services.venuebuilder.VenueBuilderService;
import com.yorku.betterticketmaster.security.AuthTokenStore;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/venuebuilder")
@RequiredArgsConstructor
public class VenueBuilderController {
    private final VenueBuilderService venueBuilderService;
    private final AuthService authService;
    private final AuthTokenStore tokenStore;
    private final VenueRepository venueRepo;
    private final LayoutRepository layoutRepo;

    private User currentUser(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
            }
        }
        String userId = tokenStore.getUserId(token);
        return userId == null ? null : authService.getCurrentUser(userId).orElse(null);
    }

    private boolean isBuilderRole(User u) {
        return u != null && (u.isAdmin() || u.isOrganizer());
    }

    @GetMapping("/venues")
    public ResponseEntity<?> listVenues(HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueRepo.findAll());
    }

    @GetMapping("/venues/{venueId}/layouts")
    public ResponseEntity<?> listLayoutsByVenue(@PathVariable String venueId, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(layoutRepo.findByVenueId(venueId));
    }

    public record CreateVenueRequest(String name, String location, String type, Integer canvasWidth, Integer canvasHeight) {}

    @PostMapping("/venues")
    public ResponseEntity<?> createVenue(@RequestBody CreateVenueRequest body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");

        VenueType t;
        try {
            t = VenueType.valueOf(body.type());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid venue type");
        }

        Venue v = venueBuilderService.createVenue(body.name(), body.location(), t);
        if (body.canvasWidth() != null && body.canvasHeight() != null) {
            v = venueBuilderService.updateVenueCanvas(v.getId(), body.canvasWidth(), body.canvasHeight());
        }
        return ResponseEntity.ok(v);
    }

    public record CreateLayoutRequest(String venueId, String layoutName, String imageUrl) {}

    @PostMapping("/layouts")
    public ResponseEntity<?> createLayout(@RequestBody CreateLayoutRequest body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        Layout created = venueBuilderService.createLayout(body.venueId(), body.layoutName(), body.imageUrl());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/layouts/{layoutId}")
    public ResponseEntity<?> loadEditor(@PathVariable String layoutId, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        VenueBuilderView view = venueBuilderService.loadEditor(layoutId);
        return ResponseEntity.ok(view);
    }

    public record UpdateLayoutImageRequest(String imageUrl) {}

    @PatchMapping("/layouts/{layoutId}/image")
    public ResponseEntity<?> updateLayoutImage(@PathVariable String layoutId, @RequestBody UpdateLayoutImageRequest body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.updateLayoutImage(layoutId, body.imageUrl()));
    }

    @PostMapping("/layouts/{layoutId}/sections")
    public ResponseEntity<?> addSection(@PathVariable String layoutId, @RequestBody SectionTemplate section, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.addSection(layoutId, section));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<?> deleteSection(@PathVariable String sectionId, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        venueBuilderService.deleteSection(sectionId);
        return ResponseEntity.ok("Deleted");
    }

    public record GeometryBody(double x, double y, double width, double height, double rotation, boolean curved, double radius, double arc) {}

    @PatchMapping("/sections/{sectionId}/geometry")
    public ResponseEntity<?> updateGeometry(@PathVariable String sectionId, @RequestBody GeometryBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(
            venueBuilderService.updateGeometry(
                sectionId,
                body.x(), body.y(), body.width(), body.height(), body.rotation(),
                body.curved(), body.radius(), body.arc()
            )
        );
    }

    public record SeatToggleBody(String row, int seatNum) {}

    @PostMapping("/sections/{sectionId}/toggleSeat")
    public ResponseEntity<?> toggleSeat(@PathVariable String sectionId, @RequestBody SeatToggleBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.toggleSeat(sectionId, body.row(), body.seatNum()));
    }

    public record RowBody(String row) {}

    @PostMapping("/sections/{sectionId}/toggleRow")
    public ResponseEntity<?> toggleRow(@PathVariable String sectionId, @RequestBody RowBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.toggleRow(sectionId, body.row()));
    }

    @PostMapping("/sections/{sectionId}/rows")
    public ResponseEntity<?> addRow(@PathVariable String sectionId, @RequestBody RowBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.addRow(sectionId, body.row()));
    }

    @DeleteMapping("/sections/{sectionId}/rows/{row}")
    public ResponseEntity<?> removeRow(@PathVariable String sectionId, @PathVariable String row, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.removeRow(sectionId, row));
    }

    public record RenameRowBody(String oldRow, String newRow) {}

    @PatchMapping("/sections/{sectionId}/rows/rename")
    public ResponseEntity<?> renameRow(@PathVariable String sectionId, @RequestBody RenameRowBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.renameRow(sectionId, body.oldRow(), body.newRow()));
    }

    public record SeatsPerRowBody(int seatsPerRow) {}

    @PatchMapping("/sections/{sectionId}/seatsPerRow")
    public ResponseEntity<?> setSeatsPerRow(@PathVariable String sectionId, @RequestBody SeatsPerRowBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.setSeatsPerRow(sectionId, body.seatsPerRow()));
    }

    public record SeatConfigBody(List<String> rows, int seatsPerRow) {}

    @PatchMapping("/sections/{sectionId}/seatConfig")
    public ResponseEntity<?> updateSeatConfig(@PathVariable String sectionId, @RequestBody SeatConfigBody body, HttpServletRequest req) {
        User u = currentUser(req);
        if (!isBuilderRole(u)) return ResponseEntity.status(403).body("Organizer/Admin required");
        return ResponseEntity.ok(venueBuilderService.updateSectionSeatConfig(sectionId, body.rows(), body.seatsPerRow()));
    }
}
