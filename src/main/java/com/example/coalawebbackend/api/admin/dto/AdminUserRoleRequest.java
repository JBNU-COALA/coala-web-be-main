package com.example.coalawebbackend.api.admin.dto;

import com.example.coalawebbackend.domain.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminUserRoleRequest(
        @NotNull UserRole role
) {
}
