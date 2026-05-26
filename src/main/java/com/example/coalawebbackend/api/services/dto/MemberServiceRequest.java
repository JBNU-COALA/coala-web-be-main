package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MemberServiceRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 50) String category,
        @Size(max = 50) String owner,
        @NotBlank @Size(max = 255) String summary,
        @NotBlank @Size(max = 500) String url,
        @Size(max = 500) String githubUrl,
        @Size(max = 500) String imageUrl,
        @Size(max = 5) List<@Size(max = 500) String> additionalImageUrls,
        @NotEmpty List<@NotBlank @Size(max = 50) String> tags,
        @Size(max = 20) String status
) {
}
