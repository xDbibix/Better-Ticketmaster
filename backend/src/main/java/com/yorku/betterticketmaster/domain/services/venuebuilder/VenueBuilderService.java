package com.yorku.betterticketmaster.domain.services.venuebuilder;

import java.util.List;

import com.yorku.betterticketmaster.core.dto.venuebuilder.VenueBuilderView;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
import com.yorku.betterticketmaster.domain.model.venue.Venue;
import com.yorku.betterticketmaster.domain.model.venue.VenueType;


/**
 * Service for managing venues, layouts, and sections (domain-level).
 */
public interface VenueBuilderService {
    /**
     * Load the editor view for a layout.
     * @param layoutId layout identifier
     * @return view model
     */
    VenueBuilderView loadEditor(String layoutId);
    /**
     * Create a venue.
     * @param name venue name
     * @param location venue location
     * @param Type venue type
     * @return created venue
     */
    Venue createVenue(String name, String location, VenueType Type);
    /**
     * Update canvas dimensions for a venue.
     * @param venueId venue identifier
     * @param canvasWidth canvas width
     * @param canvasHeight canvas height
     * @return updated venue
     */
    Venue updateVenueCanvas(String venueId, int canvasWidth, int canvasHeight);
    /**
     * Create a layout for a venue.
     * @param venueId venue identifier
     * @param name layout name
     * @param imageUrl background image URL
     * @return created layout
     */
    Layout createLayout(String venueId,String name, String imageUrl);
    /**
     * Update layout image.
     * @param layoutId layout identifier
     * @param imageUrl new image URL
     * @return updated layout
     */
    Layout updateLayoutImage(String layoutId, String imageUrl);
    /**
     * Add a section to a layout.
     * @param layoutId layout identifier
     * @param section section template
     * @return created section
     */
    SectionTemplate addSection(String layoutId, SectionTemplate section);
    /**
     * Delete a section by id.
     * @param sectionId section identifier
     */
    void deleteSection(String sectionId);
    /**
     * Update rows and seats-per-row configuration.
     * @param sectionId section identifier
     * @param rows row labels
     * @param seatsPerRow seats per row
     * @return updated section
     */
    SectionTemplate updateSectionSeatConfig(String sectionId, List<String> rows, int seatsPerRow);
    /**
     * Set seats-per-row for a section.
     * @param sectionId section identifier
     * @param seatsPerRow seats per row
     * @return updated section
     */
    SectionTemplate setSeatsPerRow(String sectionId, int seatsPerRow);
    /**
     * Update geometry for a section.
     * @param sectionId section identifier
     * @param x x-position
     * @param y y-position
     * @param width width
     * @param height height
     * @param rotation rotation degrees
     * @param curved curved flag
     * @param radius curve radius
     * @param arc curve arc
     * @return updated section
     */
    SectionTemplate updateGeometry(String sectionId,double x, double y, double width, double height, double rotation, boolean curved, double radius, double arc);
    /**
     * Toggle disabled state for a seat.
     * @param sectionId section identifier
     * @param row row label
     * @param seatNum seat number
     * @return updated section
     */
    SectionTemplate toggleSeat(String sectionId, String row, int seatNum);
    /**
     * Toggle disabled state for all seats in a row.
     * @param sectionId section identifier
     * @param row row label
     * @return updated section
     */
    SectionTemplate toggleRow(String sectionId,String row);
    /**
     * Add a row to a section.
     * @param sectionId section identifier
     * @param row row label
     * @return updated section
     */
    SectionTemplate addRow(String sectionId, String row);
    /**
     * Remove a row from a section.
     * @param sectionId section identifier
     * @param row row label
     * @return updated section
     */
    SectionTemplate removeRow(String sectionId, String row);
    /**
     * Rename a row in a section.
     * @param sectionId section identifier
     * @param oldrow old row label
     * @param newRow new row label
     * @return updated section
     */
    SectionTemplate renameRow(String sectionId,String oldrow,String newRow);

}
