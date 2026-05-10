package com.example.coalawebbackend.api.recruit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruitCommentRequest(
        @NotBlank @Size(max = 1000) String content
) {
}
