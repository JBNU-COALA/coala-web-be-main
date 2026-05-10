package com.example.coalawebbackend.api.users.dto;

import jakarta.validation.constraints.Size;

public record UserProfileRequest(
        @Size(max = 1000) String bio,
        @Size(max = 4000) String activityNote,
        @Size(max = 4000) String awardNote,
        @Size(max = 1000) String sharedRepositories
) {
}
