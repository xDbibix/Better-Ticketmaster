package com.yorku.betterticketmaster.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class AuthTokenStore {
    private final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    public void put(String token, String userId) {
        tokenToUser.put(token, userId);
    }

    public String getUserId(String token) {
        return token == null ? null : tokenToUser.get(token);
    }

    public void remove(String token) {
        if (token != null) tokenToUser.remove(token);
    }
}
