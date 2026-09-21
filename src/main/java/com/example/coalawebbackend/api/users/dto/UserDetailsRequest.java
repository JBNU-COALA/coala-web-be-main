package com.example.coalawebbackend.api.users.dto;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UserDetailsRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 50) String nickname,
        @PastOrPresent LocalDate birthDate,
        Gender gender,
        @NotBlank @Size(max = 100) String department,
        @Size(max = 150) String lab,
        @NotBlank @Size(max = 20) String studentId,
        @Min(1) @Max(6) Integer grade,
        @NotBlank @Size(max = 39) @Pattern(regexp = "[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*") String githubId,
        @Size(max = 50) @Pattern(regexp = "[A-Za-z0-9_]*") String baekjoonId,
        @Size(max = 255) @Pattern(regexp = "^(https?://[^\\s]+)?$") String linkedinUrl,
        @NotNull AcademicStatus academicStatus
) {}
