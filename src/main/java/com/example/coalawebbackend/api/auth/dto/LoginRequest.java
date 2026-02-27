package com.example.coalawebbackend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 */
public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email
        @Schema(example = "user@example.com")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Schema(example = "P@ssw0rd!")
        String password
) {}
