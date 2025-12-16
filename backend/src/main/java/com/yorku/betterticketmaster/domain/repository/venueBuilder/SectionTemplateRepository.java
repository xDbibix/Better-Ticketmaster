package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.SectionTemplate;
@Repository
public interface SectionTemplateRepository extends MongoRepository<SectionTemplate, String>{
    List<SectionTemplate> findByLayoutId(String layoutId);
}
