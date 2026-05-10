package com.example.coalawebbackend.api.recruit.dto;

import java.util.List;

public record RecruitPostResponse(
        String id,
        String title,
        String shortDesc,
        String category,
        String status,
        int currentMembers,
        int maxMembers,
        String host,
        String hostInitials,
        String hostTone,
        String hostRole,
        double trustScore,
        List<String> tags,
        List<String> techStack,
        List<RecruitRoleResponse> roles,
        String meetingType,
        String expectedDuration,
        List<String> detailContent,
        List<String> processList,
        List<RecruitCommentResponse> comments,
        String createdAt,
        long views,
        long bookmarks
) {
}
