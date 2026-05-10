package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MemberServiceRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 50) String category,
        @NotBlank @Size(max = 255) String summary,
        @NotBlank @Size(max = 500) String url,
        @NotEmpty List<@NotBlank @Size(max = 50) String> tags
) {
}
