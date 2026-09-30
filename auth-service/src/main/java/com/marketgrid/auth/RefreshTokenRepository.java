package com.marketgrid.auth;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RefreshTokenRepository {
    private final Map<String, RefreshToken> byToken = new ConcurrentHashMap<>();

    public Optional<RefreshToken> findByToken(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(byToken.get(token));
    }

    public void deleteByUserId(String userId) {
        if (userId == null) return;
        byToken.values().removeIf(rt -> userId.equals(rt.getUserId()));
    }

    public RefreshToken save(RefreshToken rt) {
        if (rt.getId() == null || rt.getId().isBlank()) {
            rt.setId(UUID.randomUUID().toString());
        }
        if (rt.getToken() != null) {
            byToken.put(rt.getToken(), rt);
        }
        return rt;
    }
}
