package com.marketgrid.auth;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class OtpVerificationRepository {
    private final List<OtpVerification> list = new CopyOnWriteArrayList<>();

    public Optional<OtpVerification> findTopByEmailOrderByExpiryTimeDesc(String email) {
        if (email == null) return Optional.empty();
        return list.stream()
                .filter(o -> email.equalsIgnoreCase(o.getEmail()))
                .max((a, b) -> a.getExpiryTime().compareTo(b.getExpiryTime()));
    }

    public void deleteByEmail(String email) {
        if (email == null) return;
        list.removeIf(o -> email.equalsIgnoreCase(o.getEmail()));
    }

    public OtpVerification save(OtpVerification ov) {
        if (ov.getId() == null || ov.getId().isBlank()) {
            ov.setId(UUID.randomUUID().toString());
        }
        list.removeIf(o -> ov.getId().equals(o.getId()));
        list.add(ov);
        return ov;
    }
}
