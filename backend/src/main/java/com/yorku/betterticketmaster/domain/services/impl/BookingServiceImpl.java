package com.yorku.betterticketmaster.domain.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yorku.betterticketmaster.domain.model.booking.Booking;
import com.yorku.betterticketmaster.domain.model.booking.Ticket;
import com.yorku.betterticketmaster.domain.repository.booking.BookingRepository;
import com.yorku.betterticketmaster.domain.repository.booking.TicketRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.SeatRepository;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.services.BookingService;
import com.yorku.betterticketmaster.domain.services.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final SeatRepository seatRepo;
    private final EventRepository eventRepo;

    @Override
    public Booking createBooking(Booking b) {
        if (b.getSeatIds() == null || b.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("seatIds is required");
        }
        b.ensurePendingInitialized();
        return bookingRepo.save(b);
    }

    @Override
    public Booking completeBooking(String bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        b.ensurePendingInitialized();
        if (!"PENDING".equals(b.getStatus())) throw new IllegalStateException("Booking not pending");
        if (b.getSeatIds() == null || b.getSeatIds().isEmpty()) {
            throw new IllegalStateException("Booking has no seatIds");
        }

        var evForBooking = eventRepo.findById(b.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));
        if (evForBooking.isClosed()) throw new IllegalStateException("Event is CLOSED");

        // If the booking itself has expired, fail fast.
        if (b.getExpiry() != null && LocalDateTime.now().isAfter(b.getExpiry())) {
            b.markExpired();
            bookingRepo.save(b);
            throw new IllegalStateException("Booking expired");
        }

        // Validate all seats are HELD and hold has not expired before creating any tickets.
        java.time.Instant now = java.time.Instant.now();
        for (String seatId : b.getSeatIds()) {
            var seat = seatRepo.findById(seatId).orElseThrow(() -> new IllegalStateException("Seat not found: " + seatId));
            if (!"HELD".equals(seat.getStatus()) || seat.getHoldUntil() == null || seat.getHoldUntil().isBefore(now)) {
                throw new IllegalStateException("Seat must be HELD and hold not expired to purchase: " + seatId);
            }
        }

        List<Ticket> created = new ArrayList<>();
        double unitPrice = b.getTotalPrice() / b.getSeatIds().size();
        java.time.LocalDateTime purchasedAt = java.time.LocalDateTime.now();
        for (String seatId : b.getSeatIds()) {
            // Mark seat SOLD first (enforced by pre-validation above).
            var seat = seatRepo.findById(seatId).orElseThrow(() -> new IllegalStateException("Seat not found: " + seatId));
            seat.sellSeat();
            seatRepo.save(seat);

            Ticket t = Ticket.purchased(b.getEventId(), seatId, b.getConsumerId(), unitPrice, purchasedAt);
            created.add(ticketRepo.save(t));
        }

        b.markCompleted();
        bookingRepo.save(b);

        // add tickets to user owned list
        var user = userRepo.findById(b.getConsumerId());
        if (user.isPresent()) {
            var u = user.get();
            for (Ticket t : created) u.addOwnedTicket(t.getId());
            userRepo.save(u);

            // Send email notification (stub logs to console)
            try {
                notificationService.sendEmail(
                    u.getEmail(),
                    "Your Ticket Purchase Confirmation",
                    "purchase-confirmation",
                    java.util.Map.of(
                        "user", u.getName(),
                        "eventId", b.getEventId(),
                        "seats", b.getSeatIds(),
                        "total", b.getTotalPrice()
                    )
                );
            } catch (Exception e) {
                System.err.println("[Notification] Failed to send email: " + e);
            }
        }

        return b;
    }

    @Override
    public Optional<Booking> getBooking(String id) {
        return bookingRepo.findById(id);
    }

    @Override
    public List<Booking> listByUser(String userId) {
        // naive: return all and filter
        List<Booking> all = bookingRepo.findAll();
        List<Booking> out = new ArrayList<>();
        for (Booking b : all) if (userId.equals(b.getConsumerId())) out.add(b);
        return out;
    }

    @Override
    public Booking requestTransfer(String bookingId, String toUserId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        // simple flag: set status to TRANSFER_REQUESTED
        b.requestTransfer();
        return bookingRepo.save(b);
    }

    @Override
    public Booking completeTransfer(String bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        if (!"TRANSFER_REQUESTED".equals(b.getStatus())) throw new IllegalStateException("No transfer requested");
        b.markCompleted();
        return bookingRepo.save(b);
    }

    @Override
    public java.util.List<Ticket> listResaleTickets(String eventId) {
        var ev = eventRepo.findById(eventId).orElse(null);
        if (ev != null && ev.isClosed()) return java.util.List.of();
        return ticketRepo.findByEventIdAndResale(eventId, true);
    }

    @Override
    public Ticket resellTicket(String ticketId, double price, String ownerId) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow();
        if (!t.getOwnerId().equals(ownerId) && !t.getBuyerId().equals(ownerId)) throw new IllegalStateException("Not owner");
        if (!t.getBuyerId().equals(ownerId)) throw new IllegalStateException("Only buyer can set resale");

        var ev = eventRepo.findById(t.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));
        if (ev.isClosed()) throw new IllegalStateException("Event is CLOSED");
        if (!ev.eventNotStarted()) throw new IllegalStateException("Event already started");
        if (!ev.validResalePrice(price)) {
            throw new IllegalStateException("Invalid resale price. Must be within organizer min/max resale range");
        }

        t.listForResale(price);
        return ticketRepo.save(t);
    }

    @Override
    public Ticket purchaseResale(String ticketId, String buyerId) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow();
        if (!t.isResale()) throw new IllegalStateException("Ticket not for resale");

        var ev = eventRepo.findById(t.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));
        if (ev.isClosed()) throw new IllegalStateException("Event is CLOSED");
        if (!ev.eventNotStarted()) throw new IllegalStateException("Event already started");

        String previousOwnerId = t.getOwnerId();
        t.completeResaleTo(buyerId);
        Ticket saved = ticketRepo.save(t);

        // update user ownership lists
        var previousOwner = userRepo.findById(previousOwnerId);
        if (previousOwner.isPresent()) {
            var u = previousOwner.get();
            u.removeOwnedTicket(t.getId());
            userRepo.save(u);
        }
        var newOwner = userRepo.findById(buyerId);
        if (newOwner.isPresent()) {
            var u2 = newOwner.get();
            u2.addOwnedTicket(t.getId());
            userRepo.save(u2);
        }
        return saved;
    }

}
