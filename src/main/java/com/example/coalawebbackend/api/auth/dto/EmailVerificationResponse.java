package com.example.coalawebbackend.api.auth.dto;

import com.example.coalawebbackend.domain.user.entity.User;

public record EmailVerificationResponse(
        String email,
        boolean verified,
        String message
) {
    public static EmailVerificationResponse from(User user, String message) {
        return new EmailVerificationResponse(user.getEmail(), user.isVerified(), message);
    }
}
