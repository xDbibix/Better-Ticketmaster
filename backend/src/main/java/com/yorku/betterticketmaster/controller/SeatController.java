package com.yorku.betterticketmaster.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.domain.model.event.Seat;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.SeatRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
    private final SeatRepository seatRepo;
    private final EventRepository eventRepo;

    private static final long HOLD_MINUTES = 5;

    private static String seatLabel(Seat seat) {
        if (seat == null) return "";
        String section = seat.getSection() == null ? "" : seat.getSection();
        String row = seat.getRow() == null ? "" : seat.getRow();
        int num = seat.getSeatNum();
        String pretty = (section.isBlank() ? "" : (section + " ")) + (row.isBlank() ? "" : row + "-") + num;
        if (pretty.isBlank()) pretty = seat.getId();
        return pretty + " (" + seat.getId() + ")";
    }

    @GetMapping
    public ResponseEntity<List<Seat>> listByEvent(@RequestParam String eventId) {
        List<Seat> seats = seatRepo.findByEventId(eventId);
        Instant now = Instant.now();
        boolean changed = false;
        for (Seat seat : seats) {
            if ("HELD".equals(seat.getStatus()) && seat.getHoldUntil() != null && seat.getHoldUntil().isBefore(now)) {
                seat.releaseSeat();
                seatRepo.save(seat);
                changed = true;
            }
        }
        // Optionally, re-fetch seats if any were changed
        if (changed) seats = seatRepo.findByEventId(eventId);
        return ResponseEntity.ok(seats);
    }
    /**
     * Hold seats for 5 minutes. Expects JSON: { seatIds: ["id1", "id2", ...] }
     */
    @PostMapping("/hold")
    public ResponseEntity<?> holdSeats(@RequestBody Map<String, List<String>> body) {
        List<String> seatIds = body.get("seatIds");
        if (seatIds == null || seatIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No seatIds provided");
        }

        // De-duplicate (keeps request idempotent and prevents accidental double-processing).
        seatIds = seatIds.stream().distinct().toList();

        // Ensure the event is not CLOSED before holding seats.
        String eventId = null;
        Instant now = Instant.now();

        record Snapshot(String status, Instant holdUntil) {}
        Map<String, Snapshot> originals = new HashMap<>();
        Map<String, Seat> seatsById = new HashMap<>();

        for (String seatId : seatIds) {
            var seat = seatRepo.findById(seatId).orElse(null);
            if (seat == null) {
                return ResponseEntity.status(404).body("Seat " + seatId + " not found");
            }
            originals.put(seatId, new Snapshot(seat.getStatus(), seat.getHoldUntil()));

            if ("HELD".equals(seat.getStatus()) && seat.getHoldUntil() != null && seat.getHoldUntil().isBefore(now)) {
                try {
                    seat.releaseSeat();
                    seatRepo.save(seat);
                } catch (Exception ignored) {
                }
            }

            seatsById.put(seatId, seat);
            if (eventId == null) eventId = seat.getEventId();
            if (eventId != null && seat.getEventId() != null && !eventId.equals(seat.getEventId())) {
                return ResponseEntity.badRequest().body("All seatIds must belong to the same event");
            }
        }
        if (eventId != null) {
            var ev = eventRepo.findById(eventId).orElse(null);
            if (ev != null && ev.isClosed()) {
                return ResponseEntity.status(409).body("Event is CLOSED");
            }
        }

        Instant holdUntil = Instant.now().plus(HOLD_MINUTES, ChronoUnit.MINUTES);

        // Validate first so we don't partially hold seats.
        for (String seatId : seatIds) {
            Seat seat = seatRepo.findById(seatId).orElse(null);
            if (seat == null) return ResponseEntity.status(404).body("Seat " + seatId + " not found");

            if ("HELD".equals(seat.getStatus()) && seat.getHoldUntil() != null && seat.getHoldUntil().isBefore(now)) {
            } else if ("SOLD".equals(seat.getStatus())) {
                return ResponseEntity.status(409).body("Seat " + seatLabel(seat) + " is SOLD");
            }
        }

        // Apply holds (or extend them) and rollback if anything fails.
        List<String> changedSeatIds = new ArrayList<>();
        try {
            for (String seatId : seatIds) {
                Seat seat = seatRepo.findById(seatId).orElseThrow(() -> new IllegalStateException("Seat not found: " + seatId));

                if ("HELD".equals(seat.getStatus()) && seat.getHoldUntil() != null && seat.getHoldUntil().isBefore(now)) {
                    seat.releaseSeat();
                }

                if ("AVAILABLE".equals(seat.getStatus())) {
                    seat.holdSeat(holdUntil);
                } else if ("HELD".equals(seat.getStatus())) {
                    seat.setHoldUntil(holdUntil);
                } else {
                    return ResponseEntity.status(409).body("Seat " + seatLabel(seat) + " is not available");
                }

                seatRepo.save(seat);
                changedSeatIds.add(seatId);
            }
        } catch (Exception e) {
            for (String changedId : changedSeatIds) {
                try {
                    var seat = seatRepo.findById(changedId).orElse(null);
                    var snap = originals.get(changedId);
                    if (seat != null && snap != null) {
                        seat.setStatus(snap.status());
                        seat.setHoldUntil(snap.holdUntil());
                        seatRepo.save(seat);
                    }
                } catch (Exception ignored) {}
            }
            return ResponseEntity.status(409).body("Could not hold selected seats: " + e.getMessage());
        }

        return ResponseEntity.ok("Seats held for " + HOLD_MINUTES + " minutes");
    }

    /**
     * Release seats immediately. Expects JSON }
     */
    @PostMapping("/release")
    public ResponseEntity<?> releaseSeats(@RequestBody Map<String, List<String>> body) {
        List<String> seatIds = body.get("seatIds");
        if (seatIds == null || seatIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No seatIds provided");
        }

        seatIds = seatIds.stream().distinct().toList();

        int released = 0;
        List<String> missing = new ArrayList<>();
        for (String seatId : seatIds) {
            var seat = seatRepo.findById(seatId).orElse(null);
            if (seat == null) {
                missing.add(seatId);
                continue;
            }
            if (!"HELD".equals(seat.getStatus())) continue;
            try {
                seat.releaseSeat();
                seatRepo.save(seat);
                released++;
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok(Map.of(
            "released", released,
            "missing", missing.size(),
            "missingSeatIds", missing
        ));
    }
}
