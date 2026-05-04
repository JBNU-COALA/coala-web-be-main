package com.example.coalawebbackend.api.auth.service;

import com.example.coalawebbackend.api.auth.dto.EmailVerificationResponse;
import com.example.coalawebbackend.api.auth.exception.AuthException;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationCodeStore codeStore;
    private final EmailVerificationMailService mailService;

    public void issue(User user) {
        if (user.isVerified()) {
            return;
        }
        String code = generateCode();
        codeStore.save(user.getEmail(), code);
        mailService.sendVerificationCode(user.getEmail(), user.getName(), code);
    }

    public EmailVerificationResponse resend(String rawEmail) {
        User user = findByEmail(rawEmail);
        if (!user.isVerified()) {
            issue(user);
        }
        return EmailVerificationResponse.from(
                user,
                user.isVerified() ? "이미 인증된 이메일입니다." : "인증 메일을 다시 보냈습니다.");
    }

    @Transactional
    public EmailVerificationResponse confirm(String rawEmail, String code) {
        User user = findByEmail(rawEmail);
        if (user.isVerified()) {
            return EmailVerificationResponse.from(user, "이미 인증된 이메일입니다.");
        }
        if (!codeStore.validate(user.getEmail(), code)) {
            throw new AuthException(ErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }

        user.markVerified();
        codeStore.delete(user.getEmail());
        return EmailVerificationResponse.from(user, "이메일 인증이 완료되었습니다.");
    }

    private User findByEmail(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
