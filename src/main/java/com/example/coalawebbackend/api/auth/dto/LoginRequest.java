package com.example.coalawebbackend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "아이디 또는 이메일은 필수입니다.")
        @Size(max = 100)
        @Schema(example = "test")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Schema(example = "test1234")
        String password
) {}
