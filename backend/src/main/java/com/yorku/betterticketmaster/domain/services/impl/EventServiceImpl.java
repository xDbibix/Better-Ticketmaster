package com.yorku.betterticketmaster.domain.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.yorku.betterticketmaster.domain.model.event.Event;
import com.yorku.betterticketmaster.domain.model.event.Seat;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.SectionType;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.SeatRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;
import com.yorku.betterticketmaster.domain.services.EventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepo;
    private final SeatRepository seatRepo;
    private final SectionTemplateRepository sectionRepo;

    private static final double DEFAULT_SEAT_PRICE = 25.0;

    private void ensureSeatsGenerated(Event event) {
        if (event == null || event.getId() == null) return;
        String layoutId = event.getLayoutId();
        if (layoutId == null || layoutId.trim().isEmpty()) return;

        // Don't regenerate if seats already exist.
        if (!seatRepo.findByEventId(event.getId()).isEmpty()) return;

        List<SectionTemplate> sections = sectionRepo.findByLayoutId(layoutId);
        if (sections == null || sections.isEmpty()) return;

        List<Seat> seats = new ArrayList<>();

        for (SectionTemplate section : sections) {
            if (section == null) continue;
            String sectionName = section.getSectionName();
            if (sectionName == null || sectionName.trim().isEmpty()) continue;

            Set<String> disabled = section.getDisabledSeats();
            if (disabled == null) disabled = java.util.Collections.emptySet();

            SectionType type = section.getSectionType();
            if (type == SectionType.GA) {
                int capacity = section.getCapacity();
                if (capacity <= 0) {
                    int rowsCount = section.getRows() == null ? 0 : section.getRows().size();
                    int seatsPerRow = Math.max(0, section.getSeatsPerRow());
                    capacity = rowsCount * seatsPerRow;
                }
                String rowLabel = "GA";
                for (int seatNum = 1; seatNum <= capacity; seatNum++) {
                    String key = rowLabel + "-" + seatNum;
                    if (disabled.contains(key)) continue;
                    seats.add(new Seat(null, event.getId(), sectionName, rowLabel, seatNum, DEFAULT_SEAT_PRICE));
                }
                continue;
            }

            List<String> rows = section.getRows();
            int seatsPerRow = Math.max(0, section.getSeatsPerRow());
            if (rows == null || rows.isEmpty() || seatsPerRow <= 0) continue;

            for (String row : rows) {
                if (row == null || row.trim().isEmpty()) continue;
                for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                    String key = row + "-" + seatNum;
                    if (disabled.contains(key)) continue;
                    seats.add(new Seat(null, event.getId(), sectionName, row, seatNum, DEFAULT_SEAT_PRICE));
                }
            }
        }

        if (!seats.isEmpty()) {
            seatRepo.saveAll(seats);
        }
    }

    @Override
    public Event createEvent(Event e) {
        Event created = eventRepo.save(e);
        ensureSeatsGenerated(created);
        return created;
    }

    @Override
    public Optional<Event> getEvent(String id) {
        return eventRepo.findById(id);
    }

    @Override
    public List<Event> search(String query) {
        // basic: return all for now
        return eventRepo.findAll();
    }

    @Override
    public Event updateEvent(Event e) {
        return eventRepo.save(e);
    }

    @Override
    public Event requestEventCreation(Event e, String organizerId) {
        e.setOrganizerId(organizerId);
        e.setStatus("PENDING");
        Event created = eventRepo.save(e);
        ensureSeatsGenerated(created);
        return created;
    }

    @Override
    public List<Event> listPendingEvents() {
        List<Event> all = eventRepo.findAll();
        List<Event> out = new java.util.ArrayList<>();
        for (Event ev : all) if ("PENDING".equals(ev.getStatus())) out.add(ev);
        return out;
    }

    @Override
    public Event approveEvent(String eventId) {
        Event e = eventRepo.findById(eventId).orElseThrow();
        e.setStatus("APPROVED");
        return eventRepo.save(e);
    }

    @Override
    public Event rejectEvent(String eventId) {
        Event e = eventRepo.findById(eventId).orElseThrow();
        e.setStatus("REJECTED");
        return eventRepo.save(e);
    }

    @Override
    public Event closeEvent(String eventId) {
        Event e = eventRepo.findById(eventId).orElseThrow();
        e.setStatus("CLOSED");
        return eventRepo.save(e);
    }

}
