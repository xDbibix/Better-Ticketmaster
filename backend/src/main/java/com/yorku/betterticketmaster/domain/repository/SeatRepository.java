package com.yorku.betterticketmaster.domain.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.event.Seat;

@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {

    /*
    All seats for an event
    Frontend use to draw seats on map
    */
    List<Seat> findByEventId(String eventId);

    /*
    All seats based off section
    Used for section filtering
     */
    List<Seat> findByEventIdSectionName(String eventId, String sectionName);

    /*
    All seats based off status
    Used for booking, filtering
    */
    List<Seat> findByEventIdStatus(String eventId, String status);
}
