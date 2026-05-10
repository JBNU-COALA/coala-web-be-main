package com.example.coalawebbackend.api.users.service;

import com.example.coalawebbackend.api.users.dto.UserDirectoryResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDirectoryService {

    private static final List<String> AVATAR_TONES = List.of("mint", "sky", "amber", "slate", "rose", "sand");

    private final UserRepository userRepository;

    public List<UserDirectoryResponse> getUsers(Long currentUserId) {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(user -> toResponse(user, currentUserId))
                .toList();
    }

    public UserDirectoryResponse getUser(Long userId, Long currentUserId) {
        return userRepository.findById(userId)
                .map(user -> toResponse(user, currentUserId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private UserDirectoryResponse toResponse(User user, Long currentUserId) {
        String githubHandle = blankToFallback(user.getGithubId(), "");
        String baekjoonHandle = blankToFallback(user.getBaekjoonId(), "");
        String academicStatus = formatAcademicStatus(user.getAcademicStatus());
        String grade = formatGrade(user);
        String lab = blankToFallback(user.getLab(), user.getDepartment());

        return new UserDirectoryResponse(
                user.getId(),
                user.getName(),
                initialOf(user.getName()),
                toneFor(user.getId()),
                formatRole(user),
                grade,
                lab,
                githubHandle,
                "https://github.com/" + githubHandle,
                formatFocus(user.getDepartment(), lab, academicStatus),
                formatJoinedAt(user.getCreatedAt()),
                List.of(),
                List.of(),
                baekjoonHandle,
                "unrated",
                0,
                0,
                0,
                List.of(),
                currentUserId != null && currentUserId.equals(user.getId())
        );
    }

    private String initialOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.trim().substring(0, 1);
    }

    private String toneFor(Long userId) {
        int index = Math.floorMod(userId == null ? 0 : userId.hashCode(), AVATAR_TONES.size());
        return AVATAR_TONES.get(index);
    }

    private String formatGrade(User user) {
        if (user.getAcademicStatus() == AcademicStatus.PROFESSOR) {
            return "교수";
        }
        if (user.getAcademicStatus() == AcademicStatus.ASSISTANT) {
            return "조교";
        }
        if (user.getAcademicStatus() == AcademicStatus.GRADUATED) {
            return "졸업생";
        }
        if (user.getAcademicStatus() == AcademicStatus.ON_LEAVE) {
            return "휴학생";
        }
        if (user.getAcademicStatus() == AcademicStatus.GENERAL) {
            return "일반";
        }
        return user.getGrade() == null ? "학년 미등록" : user.getGrade() + "학년";
    }

    private String formatAcademicStatus(AcademicStatus status) {
        if (status == AcademicStatus.PROFESSOR) {
            return "교수";
        }
        if (status == AcademicStatus.ASSISTANT) {
            return "조교";
        }
        if (status == AcademicStatus.ON_LEAVE) {
            return "휴학생";
        }
        if (status == AcademicStatus.GRADUATED) {
            return "졸업생";
        }
        if (status == AcademicStatus.GENERAL) {
            return "일반";
        }
        return "재학생";
    }

    private String formatRole(User user) {
        UserRole role = user.getRole();
        if (role == UserRole.SUPER_ADMIN) {
            return "최고 관리자";
        }
        if (role == UserRole.STAFF) {
            return "운영진";
        }
        return "일반 회원";
    }

    private String formatFocus(String department, String lab, String academicStatus) {
        String normalizedDepartment = blankToFallback(department, "소속 미등록");
        String normalizedLab = blankToFallback(lab, "");
        if (normalizedLab.isBlank() || normalizedLab.equals(normalizedDepartment)) {
            return normalizedDepartment + " · " + academicStatus;
        }
        return normalizedDepartment + " · " + normalizedLab + " · " + academicStatus;
    }

    private String formatJoinedAt(LocalDateTime createdAt) {
        return createdAt == null ? "가입일 미등록" : createdAt.toLocalDate() + " 가입";
    }

    private String blankToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
