package com.example.coalawebbackend.api.recruit.dto;

public record RecruitRoleResponse(
        String label,
        int current,
        int max
) {
}
