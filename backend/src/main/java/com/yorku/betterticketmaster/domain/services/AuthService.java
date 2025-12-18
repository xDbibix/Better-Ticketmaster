package com.yorku.betterticketmaster.domain.services;

import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.users.User;

public interface AuthService {
    /**
     * Register a new user account.
     * @param email user email
     * @param password raw password
     * @param role role name
     * @return created user
     */
    User register(String email, String password, String role);
    /**
     * Authenticate a user by credentials.
     * @param email user email
     * @param password raw password
     * @return authenticated user if successful
     */
    Optional<User> authenticate(String email, String password);
    /**
     * Logout a user session.
     * @param userId user identifier
     */
    void logout(String userId);
    /**
     * Retrieve the current user context.
     * @param userId user identifier
     * @return optional user
     */
    Optional<User> getCurrentUser(String userId);
}
