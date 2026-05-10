package com.example.coalawebbackend.api.info.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InfoArticleRequest(
        @NotBlank @Size(max = 20) String filter,
        @NotBlank @Size(max = 30) String tag,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 100) String meta,
        @NotBlank @Size(max = 50) String sourceName,
        @NotBlank @Size(max = 20) String sourceDate,
        @NotBlank @Size(max = 12000) String content,
        @Size(max = 500) String imageUrl
) {
}
