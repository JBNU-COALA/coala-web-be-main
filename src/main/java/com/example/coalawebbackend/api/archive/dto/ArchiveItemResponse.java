package com.example.coalawebbackend.api.archive.dto;

import com.example.coalawebbackend.domain.archive.entity.ArchiveItem;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record ArchiveItemResponse(
        Long id,
        String category,
        String title,
        String summary,
        String content,
        String sourceUrl,
        String repositoryUrl,
        List<String> tags,
        Long ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ArchiveItemResponse from(ArchiveItem item) {
        return new ArchiveItemResponse(
                item.getId(),
                item.getCategory().getApiValue(),
                item.getTitle(),
                item.getSummary(),
                item.getContent(),
                blankToEmpty(item.getSourceUrl()),
                blankToEmpty(item.getRepositoryUrl()),
                splitTags(item.getTags()),
                item.getOwner() == null ? null : item.getOwner().getId(),
                item.getOwnerName(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
