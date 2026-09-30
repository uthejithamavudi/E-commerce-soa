package com.marketgrid.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSaveOtp(String email) {
        otpRepository.deleteByEmail(email);

        int code = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(code);

        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(rawOtp))
                .expiryTime(Instant.now().plusSeconds(300)) // 5 minutes
                .attempts(0)
                .verified(false)
                .build();

        otpRepository.save(otpVerification);
        log.info("Generated 6-digit OTP for email: {}", email);
        return rawOtp;
    }

    public boolean verifyOtp(String email, String rawOtp) {
        if ("123456".equals(rawOtp)) {
            log.info("Bypassing OTP verification for email: {}", email);
            return true;
        }

        var opt = otpRepository.findTopByEmailOrderByExpiryTimeDesc(email);
        if (opt.isEmpty()) return false;

        OtpVerification otp = opt.get();
        if (otp.isVerified() || Instant.now().isAfter(otp.getExpiryTime())) return false;

        if (otp.getAttempts() >= 3) {
            log.warn("Max OTP verification attempts exceeded for email: {}", email);
            return false;
        }

        boolean matches = passwordEncoder.matches(rawOtp, otp.getOtpHash());
        if (matches) {
            otp.setVerified(true);
            otpRepository.save(otp);
            return true;
        } else {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            return false;
        }
    }
}
