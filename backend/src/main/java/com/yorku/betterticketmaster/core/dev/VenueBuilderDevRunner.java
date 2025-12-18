package com.yorku.betterticketmaster.core.dev;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.SectionType;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;
import com.yorku.betterticketmaster.domain.services.venuebuilder.VenueBuilderService;

import lombok.RequiredArgsConstructor;
// Lowkey had ai help me with this
/**
 * Dev runner to seed venue builder demo data and print diagnostics.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class VenueBuilderDevRunner implements CommandLineRunner {

    private final VenueBuilderService venueBuilderService;
    private final LayoutRepository layoutRepository;
    private final SectionTemplateRepository sectionRepository;

    @Override
    /**
     * Execute venue builder seeding and diagnostics in dev.
     */
    public void run(String... args) {

        System.out.println("=== VENUE BUILDER DEV TEST START ===");

        // Avoid duplicating seed data on every restart.
        // If the core layouts already exist, skip creating them again.
        boolean alreadySeeded = layoutRepository.findAll().stream().anyMatch(l ->
            "Basketball Default".equals(l.getLayoutName()) || "Symphony Default".equals(l.getLayoutName())
        );
        if (alreadySeeded) {
            System.out.println("Venue builder seed already present; skipping creation.");
            System.out.println("=== VENUE BUILDER DEV TEST END ===");
            return;
        }

        // Create Scotiabank Arena
        Venue arena = venueBuilderService.createVenue(
            "Scotiabank Arena",
            "Toronto, ON",
            VenueType.ARENA
        );
        System.out.println("Created venue: " + arena.getId());
        Layout arenaLayout = new Layout(arena.getId(), "Basketball Default", "arena.png");
        arenaLayout = layoutRepository.save(arenaLayout);
        SectionTemplate arenaSection = new SectionTemplate();
        arenaSection.setLayoutId(arenaLayout.getId());
        arenaSection.setSectionName("Lower Bowl");
        arenaSection.setRows(List.of("A", "B", "C", "D"));
        arenaSection.setSeatsPerRow(20);
        arenaSection.setSectionType(SectionType.SEATED);
        sectionRepository.save(arenaSection);
        System.out.println("Created Scotiabank Arena section: " + arenaSection.getSectionName());

        // Create Algorithm Symphony
        Venue symphony = venueBuilderService.createVenue(
            "Algorithm Symphony",
            "Toronto, ON",
            VenueType.ARENA
        );
        System.out.println("Created venue: " + symphony.getId());
        Layout symphonyLayout = new Layout(symphony.getId(), "Symphony Default", "symphony.png");
        symphonyLayout = layoutRepository.save(symphonyLayout);
        SectionTemplate symphonySection = new SectionTemplate();
        symphonySection.setLayoutId(symphonyLayout.getId());
        symphonySection.setSectionName("Main Hall");
        symphonySection.setRows(List.of("A", "B", "C"));
        symphonySection.setSeatsPerRow(30);
        symphonySection.setSectionType(SectionType.SEATED);
        sectionRepository.save(symphonySection);
        System.out.println("Created Algorithm Symphony section: " + symphonySection.getSectionName());

        // Load Venue Builder Editor for both
        VenueBuilderView view1 = venueBuilderService.loadEditor(arenaLayout.getId());
        System.out.println("Loaded editor for Scotiabank Arena:");
        System.out.println("Venue: " + view1.getVenueName());
        System.out.println("Layout: " + view1.getLayoutName());
        System.out.println("Sections: " + view1.getSections().size());

        VenueBuilderView view2 = venueBuilderService.loadEditor(symphonyLayout.getId());
        System.out.println("Loaded editor for Algorithm Symphony:");
        System.out.println("Venue: " + view2.getVenueName());
        System.out.println("Layout: " + view2.getLayoutName());
        System.out.println("Sections: " + view2.getSections().size());

        System.out.println("=== VENUE BUILDER DEV TEST END ===");
    }
}
