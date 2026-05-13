package com.example.coalawebbackend.api.auth.service;

import com.example.coalawebbackend.api.auth.dto.EmailVerificationResponse;
import com.example.coalawebbackend.api.auth.exception.AuthException;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationCodeStore codeStore;
    private final EmailVerificationMailService mailService;
    private final PasswordEncoder passwordEncoder;

    public EmailVerificationResponse issue(String rawEmail) {
        User user = findByEmail(rawEmail);
        String code = generateCode();
        codeStore.savePasswordReset(user.getEmail(), code);
        mailService.sendPasswordResetCode(user.getEmail(), user.getName(), code);
        return EmailVerificationResponse.from(user, "비밀번호 변경 인증번호를 보냈습니다.");
    }

    @Transactional
    public EmailVerificationResponse confirm(String rawEmail, String code, String newPassword) {
        User user = findByEmail(rawEmail);
        if (!codeStore.validatePasswordReset(user.getEmail(), code)) {
            throw new AuthException(ErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        codeStore.deletePasswordReset(user.getEmail());
        return EmailVerificationResponse.from(user, "비밀번호가 변경되었습니다.");
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
