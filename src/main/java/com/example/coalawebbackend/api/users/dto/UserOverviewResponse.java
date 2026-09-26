package com.example.coalawebbackend.api.users.dto;

import java.util.List;

public record UserOverviewResponse(boolean isSelf, List<UserActivityItemResponse> items, Counts counts) {
    public record Counts(long studyGroups, long studyRecords, long authoredPosts, long infoArticles,
                         long recruits, long services, Long recruitApplications, Long pendingRecruitApplications,
                         Long savedRecruits, Long instanceApplications, Long domainApplications) {}
}
