package com.yorku.betterticketmaster.domain.services;

import java.util.List;
import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.event.Event;

public interface EventService {
    /**
     * Create a new event.
     * @param e event to create
     * @return created event
     */
    Event createEvent(Event e);
    /**
     * Get an event by its unique identifier.
     * @param id event id
     * @return optional event
     */
    Optional<Event> getEvent(String id);
    /**
     * Search events by keyword.
     * @param query free-text query
     * @return matching events
     */
    List<Event> search(String query);
    /**
     * Update an existing event.
     * @param e updated event
     * @return persisted event
     */
    Event updateEvent(Event e);
    /**
     * Submit an event for approval.
     * @param e event to request
     * @param organizerId organizer submitting the request
     * @return requested event
     */
    Event requestEventCreation(Event e, String organizerId);
    /**
     * List all events awaiting approval.
     * @return pending events
     */
    List<Event> listPendingEvents();
    /**
     * Approve a pending event.
     * @param eventId event identifier
     * @return approved event
     */
    Event approveEvent(String eventId);
    /**
     * Reject a pending event.
     * @param eventId event identifier
     * @return rejected event
     */
    Event rejectEvent(String eventId);
    /**
     * Close an event to further sales.
     * @param eventId event identifier
     * @return closed event
     */
    Event closeEvent(String eventId);
}
