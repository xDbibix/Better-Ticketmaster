package com.yorku.betterticketmaster.domain.repository.booking;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.booking.Ticket;


/**
 * Repository for tickets.
 */
@Repository
public interface TicketRepository extends MongoRepository<Ticket, String>{
    /**
     * List tickets by owner.
     * @param ownerId user identifier
     * @return tickets
     */
    List<Ticket> findByOwnerId(String ownerId);
    /**
     * List tickets by event and resale flag.
     * @param eventId event identifier
     * @param resale resale flag
     * @return tickets
     */
    List<Ticket> findByEventIdAndResale(String eventId, boolean resale);
}
