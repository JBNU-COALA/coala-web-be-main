package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.Size;

public record InstanceApplicationUpdateRequest(
        @Size(max = 20) String instanceType,
        @Size(max = 20) String duration,
        @Size(max = 5000) String purpose,
        @Size(max = 20) String status,
        @Size(max = 5000) String adminNote
) {
}
