package com.yorku.betterticketmaster.domain.services;

import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.users.User;

public interface AuthService {
    User register(String email, String password, String role);
    Optional<User> authenticate(String email, String password);
    void logout(String userId);
    Optional<User> getCurrentUser(String userId);
}
