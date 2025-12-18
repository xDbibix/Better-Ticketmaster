package com.yorku.betterticketmaster.domain.repository.venueBuilder;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.venue.Venue;

@Repository
public interface VenueRepository extends MongoRepository<Venue, String>{
	List<Venue> findByVenueName(String venueName);
}
