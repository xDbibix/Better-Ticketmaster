package com.yorku.betterticketmaster.domain.services;

import java.util.Optional;

import com.yorku.betterticketmaster.domain.model.users.User;

public interface UserService {
    Optional<User> findByEmail(String email);
    User createUser(User u);
    Optional<User> findById(String id);
    User updateUser(User u);
}