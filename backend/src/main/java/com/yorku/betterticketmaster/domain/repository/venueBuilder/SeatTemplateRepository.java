package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.SeatTemplate;
@Repository
public interface SeatTemplateRepository extends MongoRepository<SeatTemplate, String>{
    List<SeatTemplate> findByLayoutId(String layoutId);
}
