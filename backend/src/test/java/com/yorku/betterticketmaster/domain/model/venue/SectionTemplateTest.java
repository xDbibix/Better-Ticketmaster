package com.yorku.betterticketmaster.domain.model.venue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SectionTemplateTest {

    @Test
    void gettersSettersWork() {
        SectionTemplate s = new SectionTemplate();
        s.setId("s1");
        s.setLayoutId("l1");
        s.setSectionName("Lower");
        s.setSectionType(SectionType.SEATED);
        s.setX(10);
        s.setY(20);
        s.setWidth(100);
        s.setHeight(50);
        s.setRotation(30);
        s.setCurved(true);
        s.setRadius(5);
        s.setArc(90);
        s.setRows(List.of("A","B"));
        s.setSeatsPerRow(10);
        s.setCapacity(200);
        s.setDisabledSeats(new HashSet<>(Set.of("A-1","B-2")));

        assertEquals("s1", s.getId());
        assertEquals("l1", s.getLayoutId());
        assertEquals("Lower", s.getSectionName());
        assertEquals(SectionType.SEATED, s.getSectionType());
        assertEquals(10, s.getX());
        assertEquals(20, s.getY());
        assertEquals(100, s.getWidth());
        assertEquals(50, s.getHeight());
        assertEquals(30, s.getRotation());
        assertTrue(s.isCurved());
        assertEquals(5, s.getRadius());
        assertEquals(90, s.getArc());
        assertEquals(List.of("A","B"), s.getRows());
        assertEquals(10, s.getSeatsPerRow());
        assertEquals(200, s.getCapacity());
        assertTrue(s.getDisabledSeats().contains("A-1"));
    }
}
