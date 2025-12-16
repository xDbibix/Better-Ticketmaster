package com.yorku.betterticketmaster.core.service.venuebuilder;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;


public interface VenueBuilderService {
    VenueBuilderView loadEditor(String layoutId);
    Layout createLayout(String venueId,String name, String imageUrl);
    SectionTemplate addSection(String layoutId, SectionTemplate section);
    SectionTemplate updateGeometry(String sectionId,double x, double y, double width, double height, double rotation, boolean curved, double radius, double arc);
    SectionTemplate toggleSeat(String sectionId, String row, int seatNum);
    SectionTemplate toggleRow(String sectionId,String row);
    SectionTemplate addRow(String sectionId, String row);
    SectionTemplate removeRow(String sectionId, String row);
    SectionTemplate renameRow(String sectionId,String oldrow,String newRow);

}
