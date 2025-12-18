package com.yorku.betterticketmaster.domain.repository.inventory;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.event.Event;

/**
 * Repository for events.
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    /**
     * Find event by title.
     * @param title event title
     * @return optional event
     */
    Optional<Event> findByTitle(String title);
}
