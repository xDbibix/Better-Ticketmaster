package com.yorku.betterticketmaster.domain.services.venuebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.VenueRepository;

import lombok.RequiredArgsConstructor;
/**
 * Domain venue builder implementation providing validation and defaults.
 */
@Service
@RequiredArgsConstructor
public class VenueBuilderImplementation implements VenueBuilderService{
    private static final int DEFAULT_CANVAS_WIDTH = 1200;
    private static final int DEFAULT_CANVAS_HEIGHT = 800;
    private final VenueRepository venueRepo;
    private final LayoutRepository layoutRepo;
    private final SectionTemplateRepository sectionRepo;

    @Override
    public VenueBuilderView loadEditor(String layoutId) {
        Layout layout = layoutRepo.findById(layoutId).orElseThrow();
        Venue venue = venueRepo.findById(layout.getVenueId()).orElseThrow();
        List<SectionTemplate> sections = sectionRepo.findByLayoutId(layoutId);

        int canvasWidth = venue.getCanvasWidth() > 0 ? venue.getCanvasWidth() : DEFAULT_CANVAS_WIDTH;
        int canvasHeight = venue.getCanvasHeight() > 0 ? venue.getCanvasHeight() : DEFAULT_CANVAS_HEIGHT;

    return VenueBuilderView.builder()
            .venueId(venue.getId())
            .venueName(venue.getVenueName())
            .layoutId(layout.getId())
            .layoutName(layout.getLayoutName())
            .canvasWidth(canvasWidth)
            .canvasHeight(canvasHeight)
            .backgroundImageUrl(layout.getImageUrl())
            .sections(sections)
            .build();
    }
    @Override
    public Venue createVenue(String venueName,String location, VenueType venueType){
        Venue v = new Venue();
        v.setVenueName(venueName);
        v.setLocation(location);
        v.setVenueType(venueType);
        // Defaults for newly created venues (in case DB/docs omit these fields)
        if (v.getCanvasWidth() <= 0) v.setCanvasWidth(DEFAULT_CANVAS_WIDTH);
        if (v.getCanvasHeight() <= 0) v.setCanvasHeight(DEFAULT_CANVAS_HEIGHT);
        return venueRepo.save(v);
    }

    @Override
    public Venue updateVenueCanvas(String venueId, int canvasWidth, int canvasHeight) {
        Venue v = venueRepo.findById(venueId).orElseThrow();
        v.setCanvasWidth(canvasWidth);
        v.setCanvasHeight(canvasHeight);
        return venueRepo.save(v);
    }

    @Override
    public Layout createLayout(String venueId, String name, String ImageUrl) {
        Layout l = new Layout(venueId, name, ImageUrl);
        return layoutRepo.save(l);
    }

    @Override
    public Layout updateLayoutImage(String layoutId, String imageUrl) {
        Layout l = layoutRepo.findById(layoutId).orElseThrow();
        l.setImageUrl(imageUrl);
        return layoutRepo.save(l);
    }

    @Override
    public SectionTemplate addSection(String layoutId, SectionTemplate s) {
        s.setLayoutId(layoutId);
        if (s.getDisabledSeats() == null) s.setDisabledSeats(new HashSet<>());
        return sectionRepo.save(s);
    }

    @Override
    public void deleteSection(String sectionId) {
        sectionRepo.deleteById(sectionId);
    }

    @Override
    public SectionTemplate updateSectionSeatConfig(String sectionId, List<String> rows, int seatsPerRow) {
        if (rows == null || rows.isEmpty()) throw new IllegalArgumentException("rows is required");
        if (seatsPerRow <= 0) throw new IllegalArgumentException("seatsPerRow must be > 0");

        SectionTemplate s = sectionRepo.findById(sectionId).orElseThrow();
        s.setRows(rows);
        s.setSeatsPerRow(seatsPerRow);
        // Clean up any disabled seats now out of bounds.
        if (s.getDisabledSeats() != null) {
            s.getDisabledSeats().removeIf(key -> seatNumberFromKey(key) > seatsPerRow);
        }
        return sectionRepo.save(s);
    }

    @Override
    public SectionTemplate setSeatsPerRow(String sectionId, int seatsPerRow) {
        if (seatsPerRow <= 0) throw new IllegalArgumentException("seatsPerRow must be > 0");
        SectionTemplate s = sectionRepo.findById(sectionId).orElseThrow();
        s.setSeatsPerRow(seatsPerRow);
        if (s.getDisabledSeats() != null) {
            s.getDisabledSeats().removeIf(key -> seatNumberFromKey(key) > seatsPerRow);
        }
        return sectionRepo.save(s);
    }

    private static int seatNumberFromKey(String key) {
        if (key == null) return -1;
        int dash = key.lastIndexOf('-');
        if (dash < 0 || dash == key.length() - 1) return -1;
        try {
            return Integer.parseInt(key.substring(dash + 1));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    @Override
    public SectionTemplate updateGeometry(String id, double x, double y, double w, double h,double r, boolean curved,double radius, double arc) {
        SectionTemplate s = sectionRepo.findById(id).orElseThrow();
        s.setX(x);
        s.setY(y);
        s.setWidth(w);
        s.setHeight(h);
        s.setRotation((r % 360 + 360) % 360);
        s.setCurved(curved);
        s.setRadius(radius);
        s.setArc(arc);
        return sectionRepo.save(s);
    }

    @Override
    public SectionTemplate toggleSeat(String id, String row, int seat) {
        SectionTemplate s = sectionRepo.findById(id).orElseThrow();
        String key = row + "-" + seat;
        if (s.getDisabledSeats().contains(key)) s.getDisabledSeats().remove(key);
        else s.getDisabledSeats().add(key);
        return sectionRepo.save(s);
    }

    @Override
    public SectionTemplate toggleRow(String id, String row) {
        SectionTemplate s = sectionRepo.findById(id).orElseThrow();
        for (int i = 1; i <= s.getSeatsPerRow(); i++) {
            String key = row + "-" + i;
            if (s.getDisabledSeats().contains(key)) s.getDisabledSeats().remove(key);
            else s.getDisabledSeats().add(key);
        }
        return sectionRepo.save(s);
    }

    @Override
    public SectionTemplate addRow(String id, String row) {
        SectionTemplate s = sectionRepo.findById(id).orElseThrow();
        s.getRows().add(row);
        return sectionRepo.save(s);
    }

    @Override
    public SectionTemplate removeRow(String id, String row) {
        SectionTemplate s = sectionRepo.findById(id).orElseThrow();
        s.getRows().remove(row);
        s.getDisabledSeats().removeIf(k -> k.startsWith(row + "-"));
        return sectionRepo.save(s);
    }

    @Override
    public SectionTemplate renameRow(String id, String oldLabel, String newLabel) {
        SectionTemplate s = sectionRepo.findById(id).orElseThrow();
        int idx = s.getRows().indexOf(oldLabel);
        s.getRows().set(idx, newLabel);

        Set<String> migrated = new HashSet<>();
        for (String k : s.getDisabledSeats()) {
            if (k.startsWith(oldLabel + "-"))
                migrated.add(k.replace(oldLabel + "-", newLabel + "-"));
            else migrated.add(k);
        }
        s.setDisabledSeats(migrated);
        return sectionRepo.save(s);
    }

}
