package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DomainApplicationRequest(
        @NotBlank @Size(max = 50) String applicantName,
        @NotBlank @Size(max = 20) String studentId,
        @NotBlank @Size(max = 120) String contactEmail,
        @NotBlank @Size(max = 100) String serviceName,
        @NotBlank @Size(min = 3, max = 60) @Pattern(regexp = "^[a-z0-9-]+$") String desiredAddress,
        @NotBlank @Size(max = 500) String repositoryUrl,
        @Size(max = 500) String targetUrl,
        @NotBlank @Size(max = 5000) String purpose
) {
}
