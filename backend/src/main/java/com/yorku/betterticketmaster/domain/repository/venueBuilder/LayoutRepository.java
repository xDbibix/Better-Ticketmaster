package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.Layout;
/**
 * Repository for layouts.
 */
@Repository
public interface LayoutRepository extends MongoRepository<Layout, String>{
    /**
     * List layouts by venue.
     * @param venueId venue identifier
     * @return layouts
     */
    List<Layout> findByVenueId(String venueId);

    /**
     * Find layout by venue and name.
     * @param venueId venue identifier
     * @param layoutName layout name
     * @return optional layout
     */
    Optional<Layout> findByVenueIdAndLayoutName(String venueId, String layoutName);
}
