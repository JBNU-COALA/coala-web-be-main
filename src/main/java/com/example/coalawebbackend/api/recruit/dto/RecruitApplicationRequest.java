package com.example.coalawebbackend.api.recruit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruitApplicationRequest(
        @NotBlank @Size(max = 100) String role,
        @NotBlank @Size(max = 12000) String body
) {
}
