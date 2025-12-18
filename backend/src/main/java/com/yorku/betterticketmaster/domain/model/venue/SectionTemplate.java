package com.yorku.betterticketmaster.domain.model.venue;

import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * Represents a SECTION
 */
@Document(collection="section_templates")
@Data
public class SectionTemplate {
    @Id
    private String id;
    private String layoutId;
    private String sectionName;
    private SectionType sectionType;
    // GEOMETRY
    private double x;
    private double y;
    private double width;
    private double height;
    private double rotation;
    private boolean curved;
    private double radius;
    private double arc;

    // SEAT GEN
    private List<String> rows;
    private int seatsPerRow;

    private int capacity;
    //FORMAT = ROW-SEATNUM eg, A-1, G1-4
    private Set<String> disabledSeats;
}
