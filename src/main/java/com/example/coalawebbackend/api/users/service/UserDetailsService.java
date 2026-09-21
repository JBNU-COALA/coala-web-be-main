package com.example.coalawebbackend.api.users.service;

import com.example.coalawebbackend.api.user.dto.UserResponse;
import com.example.coalawebbackend.api.users.dto.UserDetailsRequest;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.moderation.entity.*;
import com.example.coalawebbackend.domain.moderation.service.AdminAuditService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDetailsService {
    private final UserRepository users;
    private final PermissionService permissions;
    private final AdminAuditService audit;

    @Transactional(readOnly = true)
    public UserResponse getAccount(Long id) {
        return UserResponse.from(find(id));
    }

    public UserResponse updateAsAdmin(Long adminId, Long targetId, UserDetailsRequest request,
            HttpServletRequest httpRequest) {
        User admin = find(adminId);
        permissions.assertModerator(admin);
        User target = find(targetId);
        if (target.getRole().canModerate() && !admin.getRole().atLeast(UserRole.SUPER_ADMIN)
                && !admin.getId().equals(target.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        apply(target, request);
        audit.log(admin, ModerationTargetType.USER, targetId, AdminActionType.UPDATE_USER_PROFILE,
                "Updated member details", httpRequest);
        return UserResponse.from(target);
    }

    public void apply(User user, UserDetailsRequest request) {
        String studentId = request.studentId().trim();
        String githubId = request.githubId().trim();
        String linkedin = blank(request.linkedinUrl());
        String nickname = blank(request.nickname());
        if (!studentId.equals(user.getStudentId()) && users.existsByStudentId(studentId)) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_ID);
        }
        if (!githubId.equalsIgnoreCase(user.getGithubId()) && users.existsByGithubId(githubId)) {
            throw new CustomException(ErrorCode.DUPLICATE_GITHUB_ID);
        }
        if (linkedin != null && !linkedin.equals(user.getLinkedinUrl()) && users.existsByLinkedinUrl(linkedin)) {
            throw new CustomException(ErrorCode.DUPLICATE_LINKEDIN_URL);
        }
        if (nickname != null && users.existsByNicknameAndIdNot(nickname, user.getId())) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
        // Login identity, password, verification and privileges are never taken from this payload.
        user.updateAccountProfile(user.getEmail(), request.name().trim(), studentId, githubId,
                request.lab(), request.gender(), request.academicStatus(), linkedin);
        user.updatePersonalDetails(nickname, request.birthDate(), request.department().trim(),
                request.grade(), request.baekjoonId());
    }

    private User find(Long id) {
        if (id == null) throw new CustomException(ErrorCode.ACCESS_DENIED);
        return users.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
