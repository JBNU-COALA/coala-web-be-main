package com.example.coalawebbackend.api.archive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record ArchiveItemRequest(
        @NotBlank @Size(max = 30) String category,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 500) String summary,
        @Size(max = 120) String labName,
        LocalDate eventDate,
        @Size(max = 30) String materialType,
        @NotBlank @Size(max = 20000) String content,
        @Size(max = 500) String sourceUrl,
        @Size(max = 500) String repositoryUrl,
        List<@NotBlank @Size(max = 40) String> tags
) {
}
