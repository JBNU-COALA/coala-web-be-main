package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InstanceApplicationRequest(
        @Size(max = 50) String applicantName,
        @Pattern(regexp = "\\d{9}", message = "학번은 9자리 숫자여야 합니다.")
        @Size(max = 20) String studentId,
        @Email
        @Size(max = 120) String keyEmail,
        @NotBlank @Size(max = 20) String instanceType,
        @NotBlank @Size(max = 20) String duration,
        @NotBlank @Size(max = 5000) String purpose
) {
}
