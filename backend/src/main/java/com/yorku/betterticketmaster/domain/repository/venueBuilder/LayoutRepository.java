package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.Layout;
@Repository
public interface LayoutRepository extends MongoRepository<Layout, String>{
    List<Layout> findByVenueId(String venueId);
}
