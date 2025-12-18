package com.yorku.betterticketmaster.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * In-memory token store mapping session tokens to user IDs.
 */
@Component
public class AuthTokenStore {
    private final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    /**
     * Store a mapping from token to user ID.
     * @param token session token
     * @param userId user identifier
     */
    public void put(String token, String userId) {
        tokenToUser.put(token, userId);
    }

    /**
     * Look up the user ID for a token.
     * @param token session token
     * @return user ID or null if not found
     */
    public String getUserId(String token) {
        return token == null ? null : tokenToUser.get(token);
    }

    /**
     * Remove a token mapping.
     * @param token session token
     */
    public void remove(String token) {
        if (token != null) tokenToUser.remove(token);
    }
}
