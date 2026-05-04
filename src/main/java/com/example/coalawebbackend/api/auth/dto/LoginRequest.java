package com.example.coalawebbackend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email
        @Size(max = 100)
        @Schema(example = "name@jbnu.ac.kr")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Schema(example = "password")
        String password
) {}
