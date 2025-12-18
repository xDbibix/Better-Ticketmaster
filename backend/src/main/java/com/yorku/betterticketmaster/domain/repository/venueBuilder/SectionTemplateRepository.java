package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
@Repository
public interface SectionTemplateRepository extends MongoRepository<SectionTemplate, String>{
    List<SectionTemplate> findByLayoutId(String layoutId);

    Optional<SectionTemplate> findByLayoutIdAndSectionName(String layoutId, String sectionName);
}
