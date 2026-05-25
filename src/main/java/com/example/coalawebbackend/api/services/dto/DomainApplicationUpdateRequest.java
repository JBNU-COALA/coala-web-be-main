package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.Size;

public record DomainApplicationUpdateRequest(
        @Size(max = 20) String status,
        @Size(max = 5000) String adminNote
) {
}
