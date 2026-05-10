package com.example.coalawebbackend.api.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SiteContentRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String description,
        List<@NotBlank @Size(max = 30) String> chips
) {
}
