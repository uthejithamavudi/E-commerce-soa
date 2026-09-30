package com.marketgrid.notification;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailNotificationRepository extends MongoRepository<EmailNotificationRecord, String> {
    List<EmailNotificationRecord> findByRecipient(String recipient);
}
