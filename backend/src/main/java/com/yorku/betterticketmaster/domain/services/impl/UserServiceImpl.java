package com.yorku.betterticketmaster.domain.services.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public User createUser(User u) {
        return userRepo.save(u);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepo.findById(id);
    }

    @Override
    public User updateUser(User u) {
        return userRepo.save(u);
    }

}
