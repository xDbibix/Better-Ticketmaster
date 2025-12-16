package com.yorku.betterticketmaster.domain.repository.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.booking.Booking;
@Repository
public interface BookingRepository extends MongoRepository<Booking, String>{
    List<Booking> findByStatus(String status, LocalDateTime now);
}
