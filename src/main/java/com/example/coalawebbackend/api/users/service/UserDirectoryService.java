package com.example.coalawebbackend.api.users.service;

import com.example.coalawebbackend.api.users.dto.UserDirectoryResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.profile.entity.PublicUserActivityLog;
import com.example.coalawebbackend.domain.profile.entity.PublicUserAward;
import com.example.coalawebbackend.domain.profile.entity.PublicUserProfile;
import com.example.coalawebbackend.domain.profile.repository.PublicUserProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDirectoryService {

    private final PublicUserProfileRepository publicUserProfileRepository;

    public List<UserDirectoryResponse> getUsers() {
        return publicUserProfileRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserDirectoryResponse getUser(Long userId) {
        return publicUserProfileRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private UserDirectoryResponse toResponse(PublicUserProfile profile) {
        return new UserDirectoryResponse(
                profile.getId(),
                profile.getName(),
                profile.getInitials(),
                profile.getTone(),
                profile.getRole(),
                profile.getGrade(),
                profile.getLab(),
                profile.getGithubHandle(),
                profile.getGithubUrl(),
                profile.getFocus(),
                profile.getRecentCommit(),
                profile.getSharedRepos(),
                profile.getLogs().stream().map(this::toActivityLogResponse).toList(),
                profile.getSolvedHandle(),
                profile.getSolvedTier(),
                profile.getSolvedCount(),
                profile.getGithubCommits(),
                profile.getTotalPoints(),
                profile.getAwards().stream().map(this::toAwardResponse).toList(),
                profile.isMe()
        );
    }

    private UserDirectoryResponse.ActivityLogResponse toActivityLogResponse(PublicUserActivityLog log) {
        return new UserDirectoryResponse.ActivityLogResponse(
                log.getPublicId(),
                log.getType(),
                log.getTitle(),
                log.getRepository(),
                log.getDescription(),
                log.getTimeLabel()
        );
    }

    private UserDirectoryResponse.UserAwardResponse toAwardResponse(PublicUserAward award) {
        return new UserDirectoryResponse.UserAwardResponse(
                award.getPublicId(),
                award.getTitle(),
                award.getOrganizer(),
                award.getRank(),
                award.getAwardedAt().toString(),
                award.getCategory(),
                award.getDescription(),
                award.getCredentialUrl()
        );
    }
}
