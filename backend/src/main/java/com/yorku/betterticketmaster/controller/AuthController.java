package com.yorku.betterticketmaster.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.services.AuthService;
import com.yorku.betterticketmaster.security.AuthTokenStore;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthTokenStore tokenStore;

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String role) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r, HttpServletResponse resp) {
        User u = authService.register(r.email(), r.password(), r.role());
        // Immediately create a session cookie so the frontend can act as "logged in"
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, u.getId());
        Cookie c = new Cookie("BTM_TOKEN", token);
        c.setHttpOnly(true);
        c.setPath("/");
        resp.addCookie(c);

        u.setPassword(null);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest l, HttpServletResponse resp) {
        Optional<User> opt = authService.authenticate(l.email(), l.password());
        if (opt.isEmpty()) return ResponseEntity.status(401).body("Invalid credentials");
        User u = opt.get();
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, u.getId());
        Cookie c = new Cookie("BTM_TOKEN", token);
        c.setHttpOnly(true);
        c.setPath("/");
        resp.addCookie(c);
        u.setPassword(null);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
        String userId = tokenStore.getUserId(token);
        if (userId == null) return ResponseEntity.status(401).body("Not authenticated");
        Optional<User> optUser = authService.getCurrentUser(userId);
        if (optUser.isEmpty()) return ResponseEntity.status(404).body("User not found");
        User u2 = optUser.get();
        u2.setPassword(null);
        return ResponseEntity.ok(u2);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req, HttpServletResponse resp) {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if ("BTM_TOKEN".equals(c.getName())) token = c.getValue();
        tokenStore.remove(token);
        Cookie c = new Cookie("BTM_TOKEN", "");
        c.setMaxAge(0);
        c.setPath("/");
        resp.addCookie(c);
        return ResponseEntity.ok("Logged out");
    }
}
