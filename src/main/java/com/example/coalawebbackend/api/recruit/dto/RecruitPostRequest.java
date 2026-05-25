package com.example.coalawebbackend.api.recruit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RecruitPostRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 300) String shortDesc,
        @NotBlank @Size(max = 30) String category,
        @NotEmpty List<RecruitRoleRequest> roles,
        @NotEmpty List<@NotBlank @Size(max = 80) String> techStack,
        @NotBlank @Size(max = 150) String meetingType,
        @NotBlank @Size(max = 80) String expectedDuration,
        @Size(max = 30) String status,
        List<@NotBlank @Size(max = 50) String> tags,
        @NotEmpty List<@NotBlank @Size(max = 2000) String> detailContent,
        @NotEmpty List<@NotBlank @Size(max = 255) String> processList
) {
    public record RecruitRoleRequest(
            @NotBlank @Size(max = 80) String label,
            int max
    ) {
    }
}
