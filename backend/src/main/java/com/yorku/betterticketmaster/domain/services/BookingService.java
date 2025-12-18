package com.yorku.betterticketmaster.domain.services;

import java.util.List;
import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.booking.Booking;

public interface BookingService {
    Booking createBooking(Booking b);
    Booking completeBooking(String bookingId);
    Optional<Booking> getBooking(String id);
    List<Booking> listByUser(String userId);
    // Transfer/resale actions
    Booking requestTransfer(String bookingId, String toUserId);
    Booking completeTransfer(String bookingId);
    // Resale ticket actions
    java.util.List<com.yorku.betterticketmaster.domain.model.booking.Ticket> listResaleTickets(String eventId);
    com.yorku.betterticketmaster.domain.model.booking.Ticket resellTicket(String ticketId, double price, String ownerId);
    com.yorku.betterticketmaster.domain.model.booking.Ticket purchaseResale(String ticketId, String buyerId);
}
