package com.example.coalawebbackend.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerificationResendRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email
        @Size(max = 100)
        String email
) {}
