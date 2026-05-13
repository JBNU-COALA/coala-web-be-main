package com.example.coalawebbackend.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email
        @Size(max = 100)
        String email,

        @NotBlank(message = "인증번호는 필수입니다.")
        @Pattern(regexp = "\\d{6}", message = "인증번호는 6자리 숫자입니다.")
        String code,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
        String newPassword
) {}
