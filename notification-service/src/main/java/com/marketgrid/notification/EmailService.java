package com.marketgrid.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final EmailNotificationRepository recordRepository;

    @Value("${marketgrid.mail.from-address:noreply@marketgrid.com}")
    private String fromAddress;

    @Value("${marketgrid.mail.from-name:MarketGrid Platform}")
    private String fromName;

    @Async
    public void sendEmailAsync(String to, String type, String customSubject, Map<String, Object> params) {
        String subject = templateService.determineSubject(type, customSubject);
        String htmlContent = templateService.buildTemplateContent(type, params);

        EmailNotificationRecord record = EmailNotificationRecord.builder()
                .recipient(to)
                .subject(subject)
                .type(type)
                .status("PENDING")
                .timestamp(Instant.now())
                .build();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            record.setStatus("SENT");
            log.info("Email sent successfully to {} [Type: {}]", to, type);

        } catch (Exception e) {
            log.warn("SMTP send failed (dry-run preview logged): {}", e.getMessage());
            record.setStatus("DRY_RUN_OR_FAILED");
            record.setErrorMessage(e.getMessage());
        }

        recordRepository.save(record);
    }
}
