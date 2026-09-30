package com.marketgrid.notification;

import com.marketgrid.notification.dto.EmailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final EmailNotificationRepository recordRepository;

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendEmailAsync(request.getTo(), request.getType(), request.getSubject(), request.getParams());
        return ResponseEntity.ok(Map.of("message", "Email dispatch queued successfully"));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<EmailNotificationRecord>> getLogs(@RequestParam(required = false) String recipient) {
        if (recipient != null && !recipient.isBlank()) {
            return ResponseEntity.ok(recordRepository.findByRecipient(recipient));
        }
        return ResponseEntity.ok(recordRepository.findAll());
    }
}
