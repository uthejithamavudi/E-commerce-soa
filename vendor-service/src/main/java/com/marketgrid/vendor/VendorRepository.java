package com.marketgrid.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends MongoRepository<Vendor, String> {
    Optional<Vendor> findByOwnerUserId(String ownerUserId);
    Optional<Vendor> findBySlug(String slug);
    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);
    boolean existsByOwnerUserId(String ownerUserId);
}
