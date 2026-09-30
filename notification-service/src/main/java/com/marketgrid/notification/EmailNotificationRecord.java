package com.marketgrid.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "email_notifications")
public class EmailNotificationRecord {
    @Id
    private String id;
    private String recipient;
    private String subject;
    private String type;
    private String status; // PENDING, SENT, DRY_RUN_OR_FAILED
    private String errorMessage;
    private Instant timestamp;
}
