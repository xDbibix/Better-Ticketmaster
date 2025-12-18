package com.yorku.betterticketmaster.core.service.venuebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.SectionTemplateRepository;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.VenueRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VenueBuilderImplementation implements VenueBuilderService{
    private final VenueRepository venueRepo;
    private final LayoutRepository layoutRepo;
    private final SectionTemplateRepository sectionRepo;

    @Override
    public VenueBuilderView loadEditor(String layoutId) {
        Layout layout = layoutRepo.findById(layoutId).orElseThrow();
        Venue venue = venueRepo.findById(layout.getVenueId()).orElseThrow();
        List<SectionTemplate> sections = sectionRepo.findByLayoutId(layoutId);

    return VenueBuilderView.builder()
            .venueId(venue.getId())
            .venueName(venue.getVenueName())
            .layoutId(layout.getId())
            .layoutName(layout.getLayoutName())
            .canvasWidth(venue.getCanvasWidth())
            .canvasHeight(venue.getCanvasHeight())
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
        return venueRepo.save(v);
    }
    @Override
    public Layout createLayout(String venueId, String name, String ImageUrl) {
        Layout l = new Layout(venueId, name, ImageUrl);
        return layoutRepo.save(l);
    }

    @Override
    public SectionTemplate addSection(String layoutId, SectionTemplate s) {
        s.setLayoutId(layoutId);
        if (s.getDisabledSeats() == null) s.setDisabledSeats(new HashSet<>());
        return sectionRepo.save(s);
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
