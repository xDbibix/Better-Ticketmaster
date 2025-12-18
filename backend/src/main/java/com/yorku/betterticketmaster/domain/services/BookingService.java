package com.yorku.betterticketmaster.domain.services;

import java.util.List;
import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.booking.Booking;

public interface BookingService {
    /**
     * Create a new pending booking.
     * @param b booking to create
     * @return created booking
     */
    Booking createBooking(Booking b);
    /**
     * Complete a booking and finalize purchase.
     * @param bookingId booking identifier
     * @return completed booking
     */
    Booking completeBooking(String bookingId);
    /**
     * Get a booking by id.
     * @param id booking identifier
     * @return optional booking
     */
    Optional<Booking> getBooking(String id);
    /**
     * List bookings for a given user.
     * @param userId user identifier
     * @return bookings for the user
     */
    List<Booking> listByUser(String userId);
    // Transfer/resale actions
    /**
     * Request transfer of a booking to another user.
     * @param bookingId booking identifier
     * @param toUserId target user identifier
     * @return booking with transfer requested
     */
    Booking requestTransfer(String bookingId, String toUserId);
    /**
     * Complete a pending transfer.
     * @param bookingId booking identifier
     * @return booking after transfer completion
     */
    Booking completeTransfer(String bookingId);
    // Resale ticket actions
    /**
     * List tickets currently offered for resale for an event.
     * @param eventId event identifier
     * @return resale tickets
     */
    java.util.List<com.yorku.betterticketmaster.domain.model.booking.Ticket> listResaleTickets(String eventId);
    /**
     * List a ticket for resale by its owner.
     * @param ticketId ticket identifier
     * @param price resale price
     * @param ownerId current owner identifier
     * @return updated ticket
     */
    com.yorku.betterticketmaster.domain.model.booking.Ticket resellTicket(String ticketId, double price, String ownerId);
    /**
     * Purchase a resale ticket.
     * @param ticketId ticket identifier
     * @param buyerId purchasing user identifier
     * @return updated ticket after purchase
     */
    com.yorku.betterticketmaster.domain.model.booking.Ticket purchaseResale(String ticketId, String buyerId);
}
