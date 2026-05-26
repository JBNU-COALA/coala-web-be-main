package com.example.coalawebbackend.api.users.dto;

import java.util.List;

public record UserDirectoryResponse(
        Long id,
        String name,
        String initials,
        String tone,
        String role,
        String grade,
        String lab,
        String githubHandle,
        String githubUrl,
        String focus,
        String bio,
        String activityNote,
        String awardNote,
        String recentCommit,
        List<String> sharedRepos,
        List<ActivityLogResponse> logs,
        String solvedHandle,
        String solvedTier,
        Integer solvedCount,
        Integer githubCommits,
        Integer totalPoints,
        List<UserAwardResponse> awards,
        UserCustomizationResponse customization,
        boolean isMe
) {

    public record ActivityLogResponse(
            String id,
            String type,
            String title,
            String repository,
            String description,
            String timeLabel
    ) {
    }

    public record UserAwardResponse(
            String awardId,
            String title,
            String organizer,
            String rank,
            String awardedAt,
            String category,
            String description,
            String credentialUrl
    ) {
    }

    public record UserCustomizationResponse(
            String avatarTone,
            String headline,
            String profileImageUrl,
            List<UserProfileLinkResponse> links
    ) {
    }

    public record UserProfileLinkResponse(
            String label,
            String url
    ) {
    }
}
