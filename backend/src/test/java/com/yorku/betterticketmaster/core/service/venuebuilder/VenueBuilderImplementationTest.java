package com.yorku.betterticketmaster.core.service.venuebuilder;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueBuilderImplementationTest {

    @Mock VenueRepository venueRepo;
    @Mock LayoutRepository layoutRepo;
    @Mock SectionTemplateRepository sectionRepo;

    @InjectMocks VenueBuilderImplementation svc;

    @Test
    void loadEditor_buildsView() {
        Layout layout = new Layout();
        layout.setId("lay1");
        layout.setVenueId("v1");
        layout.setLayoutName("Default");
        layout.setImageUrl("img");

        Venue venue = new Venue();
        venue.setId("v1");
        venue.setVenueName("Arena");
        venue.setCanvasWidth(800);
        venue.setCanvasHeight(600);

        when(layoutRepo.findById("lay1")).thenReturn(Optional.of(layout));
        when(venueRepo.findById("v1")).thenReturn(Optional.of(venue));
        when(sectionRepo.findByLayoutId("lay1")).thenReturn(List.of(new SectionTemplate()));

        VenueBuilderView view = svc.loadEditor("lay1");
        assertEquals("v1", view.getVenueId());
        assertEquals("Arena", view.getVenueName());
        assertEquals("lay1", view.getLayoutId());
        assertEquals("Default", view.getLayoutName());
        assertEquals(800, view.getCanvasWidth());
        assertEquals(600, view.getCanvasHeight());
        assertEquals("img", view.getBackgroundImageUrl());
        assertEquals(1, view.getSections().size());
    }

    @Test
    void createVenue_setsFieldsAndSaves() {
        when(venueRepo.save(any(Venue.class))).thenAnswer(inv -> inv.getArgument(0));
        Venue saved = svc.createVenue("Arena","City", VenueType.ARENA);
        assertEquals("Arena", saved.getVenueName());
        assertEquals("City", saved.getLocation());
        assertEquals(VenueType.ARENA, saved.getVenueType());
        verify(venueRepo).save(any(Venue.class));
    }

    @Test
    void createLayout_setsFieldsAndSaves() {
        when(layoutRepo.save(any(Layout.class))).thenAnswer(inv -> inv.getArgument(0));
        Layout saved = svc.createLayout("v1", "Default", "img");
        assertEquals("v1", saved.getVenueId());
        assertEquals("Default", saved.getLayoutName());
        assertEquals("img", saved.getImageUrl());
        verify(layoutRepo).save(any(Layout.class));
    }

    @Test
    void addSection_setsLayoutAndInitializesDisabledSeats() {
        SectionTemplate s = new SectionTemplate();
        s.setDisabledSeats(null);
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));
        SectionTemplate saved = svc.addSection("lay1", s);
        assertEquals("lay1", saved.getLayoutId());
        assertNotNull(saved.getDisabledSeats());
    }

    @Test
    void updateGeometry_normalizesRotation() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        when(sectionRepo.findById("s1")).thenReturn(Optional.of(s));
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));
        SectionTemplate updated = svc.updateGeometry("s1", 1,2,3,4, -10, false, 0, 0);
        assertEquals(350.0, updated.getRotation());
        assertEquals(3.0, updated.getWidth());
        assertEquals(4.0, updated.getHeight());
    }

    @Test
    void toggleSeat_addsAndRemoves() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        s.setDisabledSeats(new HashSet<>());
        when(sectionRepo.findById("s1")).thenReturn(Optional.of(s));
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        SectionTemplate afterAdd = svc.toggleSeat("s1", "A", 1);
        assertTrue(afterAdd.getDisabledSeats().contains("A-1"));
        SectionTemplate afterRemove = svc.toggleSeat("s1", "A", 1);
        assertFalse(afterRemove.getDisabledSeats().contains("A-1"));
    }

    @Test
    void toggleRow_togglesAllSeatsInRow() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        s.setDisabledSeats(new HashSet<>());
        s.setSeatsPerRow(3);
        when(sectionRepo.findById("s1")).thenReturn(Optional.of(s));
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        SectionTemplate after = svc.toggleRow("s1", "B");
        assertTrue(after.getDisabledSeats().containsAll(Set.of("B-1","B-2","B-3")));
    }

    @Test
    void addRow_appendsRow() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        s.setRows(new ArrayList<>());
        when(sectionRepo.findById("s1")).thenReturn(Optional.of(s));
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));
        SectionTemplate after = svc.addRow("s1", "Z");
        assertTrue(after.getRows().contains("Z"));
    }

    @Test
    void removeRow_removesRowAndDisabledSeats() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        s.setRows(new ArrayList<>(List.of("A","B")));
        s.setDisabledSeats(new HashSet<>(Set.of("B-1","B-2","A-1")));
        when(sectionRepo.findById("s1")).thenReturn(Optional.of(s));
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));
        SectionTemplate after = svc.removeRow("s1", "B");
        assertFalse(after.getRows().contains("B"));
        assertFalse(after.getDisabledSeats().contains("B-1"));
        assertTrue(after.getDisabledSeats().contains("A-1"));
    }

    @Test
    void renameRow_migratesDisabledSeats() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        s.setRows(new ArrayList<>(List.of("A","B")));
        s.setDisabledSeats(new HashSet<>(Set.of("B-1","B-3","A-2")));
        when(sectionRepo.findById("s1")).thenReturn(Optional.of(s));
        when(sectionRepo.save(any(SectionTemplate.class))).thenAnswer(inv -> inv.getArgument(0));
        SectionTemplate after = svc.renameRow("s1", "B", "C");
        assertEquals(List.of("A","C"), after.getRows());
        assertTrue(after.getDisabledSeats().containsAll(Set.of("C-1","C-3","A-2")));
        assertFalse(after.getDisabledSeats().contains("B-1"));
    }
}
