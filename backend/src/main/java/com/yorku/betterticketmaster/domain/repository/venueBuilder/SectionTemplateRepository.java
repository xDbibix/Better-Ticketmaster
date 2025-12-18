package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
/**
 * Repository for section templates.
 */
@Repository
public interface SectionTemplateRepository extends MongoRepository<SectionTemplate, String>{
    /**
     * List sections by layout.
     * @param layoutId layout identifier
     * @return sections
     */
    List<SectionTemplate> findByLayoutId(String layoutId);

    /**
     * Find section by layout and name.
     * @param layoutId layout identifier
     * @param sectionName section name
     * @return optional section
     */
    Optional<SectionTemplate> findByLayoutIdAndSectionName(String layoutId, String sectionName);
}
