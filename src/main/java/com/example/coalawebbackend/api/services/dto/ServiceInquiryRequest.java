package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceInquiryRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 5000) String content,
        @Size(max = 50) String author
) {
}
