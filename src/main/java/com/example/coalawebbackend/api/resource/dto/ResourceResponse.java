package com.example.coalawebbackend.api.resource.dto;

import com.example.coalawebbackend.domain.resource.entity.Resource;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ResourceResponse {

    private Long resourceId;
    private Long postId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;

    public static ResourceResponse from(Resource resource) {
        return ResourceResponse.builder()
                .resourceId(resource.getId())
                .postId(resource.getPost().getPostId())
                .fileName(resource.getFileName())
                .fileUrl(resource.getFileUrl())
                .fileType(resource.getFileType())
                .fileSize(resource.getFileSize())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
