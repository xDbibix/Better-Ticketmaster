package com.yorku.betterticketmaster.domain.repository.inventory;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.event.Event;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
	Optional<Event> findByTitle(String title);
}
