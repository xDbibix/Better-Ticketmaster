package com.yorku.betterticketmaster.domain.repository.booking;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.booking.Ticket;


@Repository
public interface TicketRepository extends MongoRepository<Ticket, String>{
    List<Ticket> findByOwnerId(String ownerId);
    List<Ticket> findByEventIdAndResale(String eventId, boolean resale);
}
