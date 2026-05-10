package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstanceApplicationRequest(
        @Size(max = 50) String applicantName,
        @Size(max = 20) String studentId,
        @Size(max = 120) String keyEmail,
        @NotBlank @Size(max = 20) String instanceType,
        @NotBlank @Size(max = 20) String duration,
        @NotBlank @Size(max = 5000) String purpose
) {
}
