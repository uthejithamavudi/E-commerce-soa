package com.marketgrid.auth;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private final Map<String, User> byId = new ConcurrentHashMap<>();
    private final Map<String, User> byEmail = new ConcurrentHashMap<>();

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(byEmail.get(email.toLowerCase()));
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return byEmail.containsKey(email.toLowerCase());
    }

    public User save(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        byId.put(user.getId(), user);
        if (user.getEmail() != null) {
            byEmail.put(user.getEmail().toLowerCase(), user);
        }
        return user;
    }

    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }
}
