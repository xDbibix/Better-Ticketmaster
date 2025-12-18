package com.yorku.betterticketmaster.domain.repository.login;

import java.util.Optional; //Use optional for if there is no matching email, used for 0 or 1 element

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yorku.betterticketmaster.domain.model.users.User;
/**
 * Repository for user accounts.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Find a user by email.
     * @param email email address
     * @return optional user
     */
    Optional<User> findByEmail(String email);
}
