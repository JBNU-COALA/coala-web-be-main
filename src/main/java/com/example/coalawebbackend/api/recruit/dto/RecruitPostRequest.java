package com.example.coalawebbackend.api.recruit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record RecruitPostRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 300) String shortDesc,
        @NotBlank @Pattern(regexp = "study|project|tutoring") String category,
        @NotEmpty @Size(max = 20) List<@NotNull @Valid RecruitRoleRequest> roles,
        @NotNull @Size(max = 30) List<@NotBlank @Size(max = 80) String> techStack,
        @NotBlank @Size(max = 150) String meetingType,
        @NotBlank @Size(max = 80) String expectedDuration,
        @Pattern(regexp = "open|closing-soon|closed") String status,
        @Size(max = 20) List<@NotBlank @Size(max = 50) String> tags,
        @NotEmpty @Size(max = 100) List<@NotBlank @Size(max = 2000) String> detailContent,
        @NotNull @Size(max = 30) List<@NotBlank @Size(max = 255) String> processList
) {
    public record RecruitRoleRequest(
            @NotBlank @Size(max = 80) String label,
            @Min(1) @Max(200) int max
    ) {
    }
}
