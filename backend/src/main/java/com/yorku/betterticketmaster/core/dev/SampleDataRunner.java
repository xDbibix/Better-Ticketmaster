package com.yorku.betterticketmaster.core.dev;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.yorku.betterticketmaster.domain.model.event.Event;
import com.yorku.betterticketmaster.domain.model.event.Seat;
import com.yorku.betterticketmaster.domain.model.users.Role;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.SectionType;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.booking.TicketRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.SeatRepository;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.VenueRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class SampleDataRunner implements CommandLineRunner {
    private final UserRepository userRepo;
    private final EventRepository eventRepo;
    private final VenueRepository venueRepo;
    private final LayoutRepository layoutRepo;
    private final SectionTemplateRepository sectionRepo;
    private final SeatRepository seatRepo;
    private final TicketRepository ticketRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // Seed demo accounts (password: "password")
        if (userRepo.findByEmail("admin@btm.test").isEmpty()) {
            User admin = new User(UUID.randomUUID().toString(), "admin@btm.test", passwordEncoder.encode("password"), "Admin", Role.ADMIN);
            userRepo.save(admin);
        }
        if (userRepo.findByEmail("organizer@btm.test").isEmpty()) {
            User org = new User(UUID.randomUUID().toString(), "organizer@btm.test", passwordEncoder.encode("password"), "Organizer", Role.ORGANIZER);
            userRepo.save(org);
        }
        if (userRepo.findByEmail("consumer@btm.test").isEmpty()) {
            User c = new User(UUID.randomUUID().toString(), "consumer@btm.test", passwordEncoder.encode("password"), "Consumer", Role.CONSUMER);
            userRepo.save(c);
        }

            final String basketballTitle = "Basketball Test";
            final String venueName = "Scotiabank Arena";
            final String layoutName = "Basketball Default";
            final String sectionName = "Lower Bowl";

            // Ensure venue exists (tolerate duplicates created by other dev seeders)
            List<Venue> existingVenues = venueRepo.findByVenueName(venueName);
            Venue v;
            if (existingVenues != null && !existingVenues.isEmpty()) {
                v = existingVenues.get(0);
            } else {
                Venue nv = new Venue();
                nv.setVenueName(venueName);
                nv.setLocation("Toronto, ON");
                nv.setVenueType(VenueType.ARENA);
                nv.setCanvasWidth(1000);
                nv.setCanvasHeight(700);
                v = venueRepo.save(nv);
            }

            // Ensure layout exists
            Layout l = layoutRepo.findByVenueIdAndLayoutName(v.getId(), layoutName).orElseGet(() -> layoutRepo.save(new Layout(v.getId(), layoutName, "arena.png")));

            // Ensure section template exists
            SectionTemplate s = sectionRepo.findByLayoutIdAndSectionName(l.getId(), sectionName).orElseGet(() -> {
                SectionTemplate ns = new SectionTemplate();
                ns.setLayoutId(l.getId());
                ns.setSectionName(sectionName);
                ns.setRows(List.of("A", "B", "C", "D"));
                ns.setSeatsPerRow(20);
                ns.setSectionType(SectionType.SEATED);
                return sectionRepo.save(ns);
            });

            // Ensure event exists
            Event e = eventRepo.findByTitle(basketballTitle).orElseGet(() -> {
                Event ne = new Event(
                    null,
                    "organizer@btm.test",
                    l.getId(),
                    basketballTitle,
                    v.getVenueName(),
                    LocalDateTime.now().plusDays(3),
                    5.0,
                    200.0,
                    "Basketball test event (seeded in dev)",
                    null
                );
                ne.setStatus("APPROVED");
                return eventRepo.save(ne);
            });

            // Ensure seats exist for the event
            if (seatRepo.findByEventId(e.getId()).isEmpty()) {
                List<Seat> seats = new ArrayList<>();
                for (String row : s.getRows()) {
                    for (int i = 1; i <= s.getSeatsPerRow(); i++) {
                        seats.add(new Seat(null, e.getId(), s.getSectionName(), row, i, 25.0));
                    }
                }
                seatRepo.saveAll(seats);
            }
    }
}
