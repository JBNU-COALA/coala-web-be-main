package com.example.coalawebbackend.api.users.dto;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserProfileRequest(
        @Size(max = 50) String name,
        @Email @Size(max = 100) String email,
        @Size(max = 20) String studentId,
        @Size(max = 39) String githubId,
        @Size(max = 150) String lab,
        Gender gender,
        AcademicStatus academicStatus,
        @Size(max = 255) String linkedinUrl,
        @Size(max = 1000) String bio,
        @Size(max = 4000) String activityNote,
        @Size(max = 4000) String awardNote,
        @Size(max = 1000) String sharedRepositories,
        @Size(max = 4000) String customization
) {
}
