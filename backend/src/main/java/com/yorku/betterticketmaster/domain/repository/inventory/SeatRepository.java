package com.yorku.betterticketmaster.domain.repository.inventory;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.event.Seat;

/**
 * Repository for seats.
 */
@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {

    /*
    All seats for an event
    Frontend use to draw seats on map
    */
    /**
     * Find all seats for an event.
     * @param eventId event identifier
     * @return seats
     */
    List<Seat> findByEventId(String eventId);

    /*
    All seats based off section
    Used for section filtering
     */
    /**
     * Find seats by event and section.
     * @param eventId event identifier
     * @param section section label
     * @return seats
     */
    List<Seat> findByEventIdAndSection(String eventId, String section);

    /*
    All seats based off status
    Used for booking, filtering
    */
    /**
     * Find seats by event and status.
     * @param eventId event identifier
     * @param status seat status
     * @return seats
     */
    List<Seat> findByEventIdAndStatus(String eventId, String status);
}
