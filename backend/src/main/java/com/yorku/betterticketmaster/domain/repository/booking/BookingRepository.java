package com.yorku.betterticketmaster.domain.repository.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.booking.Booking;
/**
 * Repository for bookings.
 */
@Repository
public interface BookingRepository extends MongoRepository<Booking, String>{
    /**
     * Find bookings by status.
     * @param status booking status
     * @param now current time (unused in query)
     * @return bookings
     */
    List<Booking> findByStatus(String status, LocalDateTime now);
}
