package com.example.coalawebbackend.api.users.service;

import com.example.coalawebbackend.api.users.dto.UserDirectoryResponse;
import com.example.coalawebbackend.api.users.dto.UserProfileRequest;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDirectoryService {

    private static final List<String> AVATAR_TONES = List.of("mint", "sky", "amber", "slate", "rose", "sand");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    @Transactional
    public UserDirectoryResponse updateMyProfile(Long currentUserId, UserProfileRequest request) {
        if (currentUserId == null) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        validateUniqueProfileFields(user, request);
        user.updateAccountProfile(
                normalizedOrCurrent(request.email(), user.getEmail()).toLowerCase(),
                normalizedOrCurrent(request.name(), user.getName()),
                normalizedOrCurrent(request.studentId(), user.getStudentId()),
                normalizedOrCurrent(request.githubId(), user.getGithubId()),
                request.lab() == null ? user.getLab() : normalizeBlank(request.lab()),
                request.gender() == null ? user.getGender() : request.gender(),
                request.academicStatus() == null ? user.getAcademicStatus() : request.academicStatus(),
                request.linkedinUrl() == null ? user.getLinkedinUrl() : normalizeBlank(request.linkedinUrl())
        );
        user.updateProfile(
                request.bio(),
                request.activityNote(),
                request.awardNote(),
                normalizeSharedRepositories(request.sharedRepositories()),
                normalizeCustomization(request.customization()));
        return toResponse(user, currentUserId);
    }

    private UserDirectoryResponse toResponse(User user, Long currentUserId) {
        String githubHandle = blankToFallback(user.getGithubId(), "");
        String baekjoonHandle = blankToFallback(user.getBaekjoonId(), "");
        String academicStatus = formatAcademicStatus(user.getAcademicStatus());
        String grade = formatGrade(user);
        String lab = blankToFallback(user.getLab(), user.getDepartment());
        UserDirectoryResponse.UserCustomizationResponse customization = parseCustomization(user.getProfileCustomization(), user);
        String headline = blankToFallback(customization.headline(), formatFocus(user.getDepartment(), lab, academicStatus));

        return new UserDirectoryResponse(
                user.getId(),
                user.getName(),
                initialOf(user.getName()),
                customization.avatarTone(),
                formatRole(user),
                grade,
                lab,
                githubHandle,
                "https://github.com/" + githubHandle,
                headline,
                blankToFallback(user.getProfileBio(), ""),
                blankToFallback(user.getProfileActivityNote(), ""),
                blankToFallback(user.getProfileAwardNote(), ""),
                formatJoinedAt(user.getCreatedAt()),
                splitSharedRepositories(user.getProfileSharedRepositories()),
                List.of(),
                baekjoonHandle,
                "unrated",
                0,
                0,
                0,
                parseAwards(user.getProfileAwardNote()),
                customization,
                currentUserId != null && currentUserId.equals(user.getId())
        );
    }

    private void validateUniqueProfileFields(User user, UserProfileRequest request) {
        String email = normalizeBlank(request.email());
        if (email != null && !email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email.toLowerCase())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String studentId = normalizeBlank(request.studentId());
        if (studentId != null && !studentId.equals(user.getStudentId()) && userRepository.existsByStudentId(studentId)) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_ID);
        }

        String githubId = normalizeBlank(request.githubId());
        if (githubId != null && !githubId.equalsIgnoreCase(user.getGithubId()) && userRepository.existsByGithubId(githubId)) {
            throw new CustomException(ErrorCode.DUPLICATE_GITHUB_ID);
        }

        String linkedinUrl = normalizeBlank(request.linkedinUrl());
        if (linkedinUrl != null
                && !Objects.equals(linkedinUrl, user.getLinkedinUrl())
                && userRepository.existsByLinkedinUrl(linkedinUrl)) {
            throw new CustomException(ErrorCode.DUPLICATE_LINKEDIN_URL);
        }
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

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizedOrCurrent(String value, String current) {
        String normalized = normalizeBlank(value);
        return normalized == null ? current : normalized;
    }

    private List<UserDirectoryResponse.UserAwardResponse> parseAwards(String value) {
        if (value == null || value.isBlank() || !value.trim().startsWith("[")) {
            return List.of();
        }

        try {
            List<StoredAward> awards = OBJECT_MAPPER.readValue(value, new TypeReference<>() {});
            return awards.stream()
                    .filter(award -> normalizeBlank(award.title()) != null)
                    .map(award -> new UserDirectoryResponse.UserAwardResponse(
                            blankToFallback(award.awardId(), "award-" + Math.abs(award.title().hashCode())),
                            award.title(),
                            blankToFallback(award.organizer(), ""),
                            blankToFallback(award.rank(), ""),
                            blankToFallback(award.awardedAt(), ""),
                            blankToFallback(award.category(), "competition"),
                            blankToFallback(award.description(), ""),
                            normalizeBlank(award.credentialUrl())
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private record StoredAward(
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

    private List<String> splitSharedRepositories(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("[\\n,]"))
                .map(String::trim)
                .filter(repo -> !repo.isBlank())
                .distinct()
                .limit(12)
                .toList();
    }

    private String normalizeSharedRepositories(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return String.join("\n", new LinkedHashSet<>(splitSharedRepositories(value)));
    }

    private UserDirectoryResponse.UserCustomizationResponse parseCustomization(String value, User user) {
        String fallbackTone = toneFor(user.getId());
        if (value == null || value.isBlank()) {
            return new UserDirectoryResponse.UserCustomizationResponse(fallbackTone, "", "", List.of());
        }

        try {
            StoredCustomization stored = OBJECT_MAPPER.readValue(value, StoredCustomization.class);
            return new UserDirectoryResponse.UserCustomizationResponse(
                    normalizeAvatarTone(stored.avatarTone(), fallbackTone),
                    blankToFallback(stored.headline(), ""),
                    blankToFallback(stored.profileImageUrl(), ""),
                    normalizeProfileLinks(stored.links())
            );
        } catch (Exception e) {
            return new UserDirectoryResponse.UserCustomizationResponse(fallbackTone, "", "", List.of());
        }
    }

    private String normalizeCustomization(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            StoredCustomization stored = OBJECT_MAPPER.readValue(value, StoredCustomization.class);
            StoredCustomization normalized = new StoredCustomization(
                    normalizeAvatarTone(stored.avatarTone(), "mint"),
                    truncate(blankToFallback(stored.headline(), ""), 120),
                    truncate(blankToFallback(stored.profileImageUrl(), ""), 500),
                    normalizeProfileLinks(stored.links()).stream()
                            .map(link -> new StoredProfileLink(link.label(), link.url()))
                            .toList()
            );
            return OBJECT_MAPPER.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private String normalizeAvatarTone(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase();
        return AVATAR_TONES.contains(normalized) ? normalized : fallback;
    }

    private List<UserDirectoryResponse.UserProfileLinkResponse> normalizeProfileLinks(List<StoredProfileLink> links) {
        if (links == null || links.isEmpty()) {
            return List.of();
        }
        return links.stream()
                .map(link -> new UserDirectoryResponse.UserProfileLinkResponse(
                        truncate(blankToFallback(link.label(), ""), 40),
                        truncate(blankToFallback(link.url(), ""), 500)
                ))
                .filter(link -> !link.label().isBlank() && isHttpUrl(link.url()))
                .limit(5)
                .toList();
    }

    private boolean isHttpUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record StoredCustomization(
            String avatarTone,
            String headline,
            String profileImageUrl,
            List<StoredProfileLink> links
    ) {
    }

    private record StoredProfileLink(
            String label,
            String url
    ) {
    }
}
