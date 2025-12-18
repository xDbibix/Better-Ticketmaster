package com.yorku.betterticketmaster.domain.services;

import java.util.List;
import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.event.Event;

public interface EventService {
    Event createEvent(Event e);
    Optional<Event> getEvent(String id);
    List<Event> search(String query);
    Event updateEvent(Event e);
    Event requestEventCreation(Event e, String organizerId);
    List<Event> listPendingEvents();
    Event approveEvent(String eventId);
    Event rejectEvent(String eventId);
    Event closeEvent(String eventId);
}
