package com.example.coalawebbackend.api.moderation.dto;

import com.example.coalawebbackend.domain.moderation.entity.UserSanctionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UserSanctionRequest(
        @NotNull Long userId,
        @NotNull UserSanctionType type,
        @NotBlank @Size(max = 500) String reason,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
