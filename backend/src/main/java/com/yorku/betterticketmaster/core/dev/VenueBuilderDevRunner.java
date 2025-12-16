package com.yorku.betterticketmaster.core.dev;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.core.service.venuebuilder.VenueBuilderService;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.SectionType;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;

import lombok.RequiredArgsConstructor;
// Lowkey had ai help me with this
@Component
@Profile("dev")
@RequiredArgsConstructor
public class VenueBuilderDevRunner implements CommandLineRunner {

    private final VenueBuilderService venueBuilderService;
    private final LayoutRepository layoutRepository;
    private final SectionTemplateRepository sectionRepository;

    @Override
    public void run(String... args) {

        System.out.println("=== VENUE BUILDER DEV TEST START ===");

        // Create Venue
        Venue venue = venueBuilderService.createVenue(
                "Scotiabank Arena",
                "Toronto, ON",
                VenueType.ARENA
        );

        System.out.println("Created venue: " + venue.getId());

        // Create Layout
        Layout layout = new Layout();
        layout.setVenueId(venue.getId());
        layout.setLayoutName("Basketball Default");
        layout.setImageUrl("arena.png");

        layout = layoutRepository.save(layout);

        System.out.println("Created layout: " + layout.getId());

        // Create Section Template
        SectionTemplate section = new SectionTemplate();
        section.setLayoutId(layout.getId());
        section.setSectionName("Lower Bowl");
        section.setRows(List.of("A", "B", "C", "D"));
        section.setSeatsPerRow(20);
        section.setSectionType(SectionType.SEATED);

        sectionRepository.save(section);

        System.out.println("Created section: " + section.getSectionName());

        // Load Venue Builder Editor
        VenueBuilderView view = venueBuilderService.loadEditor(layout.getId());

        System.out.println("Loaded editor:");
        System.out.println("Venue: " + view.getVenueName());
        System.out.println("Layout: " + view.getLayoutName());
        System.out.println("Sections: " + view.getSections().size());

        System.out.println("=== VENUE BUILDER DEV TEST END ===");
    }
}
