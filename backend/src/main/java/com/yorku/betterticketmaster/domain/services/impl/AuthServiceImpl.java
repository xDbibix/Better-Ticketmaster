package com.yorku.betterticketmaster.domain.services.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.yorku.betterticketmaster.domain.model.users.Role;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.services.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String email, String password, String role) {
        if (userRepo.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already used");
        Role r = Role.CONSUMER;
        try { r = Role.valueOf(role); } catch(Exception e) {}
        User u = new User(UUID.randomUUID().toString(), email, passwordEncoder.encode(password), email, r, new java.util.ArrayList<>());
        return userRepo.save(u);
    }

    @Override
    public Optional<User> authenticate(String email, String password) {
        var opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) return Optional.empty();
        var u = opt.get();

        // Normal path: BCrypt
        if (u.verifyPassword(password)) return Optional.of(u);

        // Back-compat: some older seed data stored plaintext passwords
        // If it matches, migrate to BCrypt so future logins work securely
        if (password != null && u.getPassword() != null && password.equals(u.getPassword())) {
            u.setPassword(passwordEncoder.encode(password));
            userRepo.save(u);
            return Optional.of(u);
        }

        return Optional.empty();
    }

    @Override
    public void logout(String userId) {
        // stateless JWT logout is token handling
    }

    @Override
    public Optional<User> getCurrentUser(String userId) {
        return userRepo.findById(userId);
    }

}
