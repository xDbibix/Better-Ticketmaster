package com.yorku.betterticketmaster.domain.services;

import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.users.User;

public interface UserService {
    /**
     * Find a user by email.
     * @param email user email
     * @return optional user
     */
    Optional<User> findByEmail(String email);
    /**
     * Create a new user.
     * @param u user to create
     * @return created user
     */
    User createUser(User u);
    /**
     * Find a user by id.
     * @param id user identifier
     * @return optional user
     */
    Optional<User> findById(String id);
    /**
     * Update an existing user.
     * @param u user with changes
     * @return persisted user
     */
    User updateUser(User u);
}