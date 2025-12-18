package com.yorku.betterticketmaster.domain.services.venuebuilder;

import java.util.List;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;


public interface VenueBuilderService {
    VenueBuilderView loadEditor(String layoutId);
    Venue createVenue(String name, String location, VenueType Type);
    Venue updateVenueCanvas(String venueId, int canvasWidth, int canvasHeight);
    Layout createLayout(String venueId,String name, String imageUrl);
    Layout updateLayoutImage(String layoutId, String imageUrl);
    SectionTemplate addSection(String layoutId, SectionTemplate section);
    void deleteSection(String sectionId);
    SectionTemplate updateSectionSeatConfig(String sectionId, List<String> rows, int seatsPerRow);
    SectionTemplate setSeatsPerRow(String sectionId, int seatsPerRow);
    SectionTemplate updateGeometry(String sectionId,double x, double y, double width, double height, double rotation, boolean curved, double radius, double arc);
    SectionTemplate toggleSeat(String sectionId, String row, int seatNum);
    SectionTemplate toggleRow(String sectionId,String row);
    SectionTemplate addRow(String sectionId, String row);
    SectionTemplate removeRow(String sectionId, String row);
    SectionTemplate renameRow(String sectionId,String oldrow,String newRow);

}
