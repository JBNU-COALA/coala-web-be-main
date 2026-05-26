package com.example.coalawebbackend.api.recruit.service;

import com.example.coalawebbackend.api.notification.service.NotificationService;
import com.example.coalawebbackend.api.recruit.dto.RecruitApplicationRequest;
import com.example.coalawebbackend.api.recruit.dto.RecruitApplicationResponse;
import com.example.coalawebbackend.api.recruit.dto.RecruitCommentRequest;
import com.example.coalawebbackend.api.recruit.dto.RecruitCommentResponse;
import com.example.coalawebbackend.api.recruit.dto.RecruitPostRequest;
import com.example.coalawebbackend.api.recruit.dto.RecruitPostResponse;
import com.example.coalawebbackend.api.recruit.dto.RecruitRoleResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.recruit.entity.RecruitApplication;
import com.example.coalawebbackend.domain.recruit.entity.RecruitBookmark;
import com.example.coalawebbackend.domain.recruit.entity.RecruitComment;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.entity.RecruitRole;
import com.example.coalawebbackend.domain.recruit.repository.RecruitApplicationRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitBookmarkRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitCommentRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitPostRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final RecruitPostRepository recruitPostRepository;
    private final RecruitCommentRepository recruitCommentRepository;
    private final RecruitApplicationRepository recruitApplicationRepository;
    private final RecruitBookmarkRepository recruitBookmarkRepository;
    private final UserService userService;
    private final PermissionService permissionService;
    private final NotificationService notificationService;

    public List<RecruitPostResponse> getRecruits(String category, String status, String query, String sort) {
        List<RecruitPost> recruits = StringUtils.hasText(category) && !"all".equalsIgnoreCase(category)
                ? recruitPostRepository.findByCategoryOrderByCreatedAtDesc(category)
                : recruitPostRepository.findAllByOrderByCreatedAtDesc();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        return recruits.stream()
                .filter(recruit -> !StringUtils.hasText(status)
                        || "all".equalsIgnoreCase(status)
                        || recruit.getStatus().equalsIgnoreCase(status))
                .filter(recruit -> normalizedQuery.isBlank() || matches(recruit, normalizedQuery))
                .sorted((left, right) -> {
                    if ("popular".equalsIgnoreCase(sort)) {
                        return Long.compare(right.getViews() + right.getBookmarks(), left.getViews() + left.getBookmarks());
                    }
                    return 0;
                })
                .map(this::toPostResponse)
                .toList();
    }

    @Transactional
    public RecruitPostResponse getRecruit(String recruitId) {
        RecruitPost recruit = getRecruitEntity(recruitId);
        recruit.increaseViews();
        return toPostResponse(recruit);
    }

    @Transactional
    public RecruitPostResponse createRecruit(RecruitPostRequest request, String userId) {
        User user = userService.findById(userId);
        List<RecruitPostRequest.RecruitRoleRequest> roleRequests = request.roles();
        int maxMembers = roleRequests.stream().mapToInt(role -> Math.max(role.max(), 1)).sum();
        RecruitPost recruit = RecruitPost.builder()
                .id(generateRecruitId(request.title()))
                .title(request.title().trim())
                .shortDesc(request.shortDesc().trim())
                .category(request.category())
                .status(normalizeStatus(request.status(), "open"))
                .currentMembers(0)
                .maxMembers(maxMembers)
                .host(displayName(user))
                .hostInitials(displayName(user).substring(0, 1))
                .hostTone("mint")
                .hostRole("모집 작성자")
                .author(user)
                .trustScore(88.0)
                .tags(normalizeTags(request.tags()))
                .techStack(request.techStack())
                .meetingType(request.meetingType())
                .expectedDuration(request.expectedDuration())
                .detailContent(request.detailContent())
                .processList(request.processList())
                .views(0)
                .bookmarks(0)
                .build();
        for (int i = 0; i < roleRequests.size(); i++) {
            RecruitPostRequest.RecruitRoleRequest role = roleRequests.get(i);
            recruit.addRole(RecruitRole.builder()
                    .label(role.label())
                    .current(0)
                    .max(Math.max(role.max(), 1))
                    .sortOrder(i)
                    .build());
        }
        return toPostResponse(recruitPostRepository.save(recruit));
    }

    @Transactional
    public RecruitPostResponse updateRecruit(User actor, String recruitId, RecruitPostRequest request) {
        RecruitPost recruit = getRecruitEntity(recruitId);
        assertCanManageRecruit(actor, recruit);
        String previousStatus = recruit.getStatus();
        List<RecruitPostRequest.RecruitRoleRequest> roleRequests = request.roles();
        int maxMembers = roleRequests.stream().mapToInt(role -> Math.max(role.max(), 1)).sum();
        recruit.update(
                request.title().trim(),
                request.shortDesc().trim(),
                request.category(),
                normalizeStatus(request.status(), recruit.getStatus()),
                maxMembers,
                normalizeTags(request.tags()),
                normalizeList(request.techStack()),
                request.meetingType().trim(),
                request.expectedDuration().trim(),
                normalizeList(request.detailContent()),
                normalizeList(request.processList())
        );
        recruit.clearRoles();
        for (int i = 0; i < roleRequests.size(); i++) {
            RecruitPostRequest.RecruitRoleRequest role = roleRequests.get(i);
            recruit.addRole(RecruitRole.builder()
                    .label(role.label().trim())
                    .current(0)
                    .max(Math.max(role.max(), 1))
                    .sortOrder(i)
                    .build());
        }
        if (!"closing-soon".equalsIgnoreCase(previousStatus)
                && "closing-soon".equalsIgnoreCase(recruit.getStatus())) {
            notificationService.notifyRecruitClosingSoon(recruit);
        }
        return toPostResponse(recruit);
    }

    public List<RecruitCommentResponse> getComments(String recruitId) {
        return recruitCommentRepository.findByRecruitPost_IdOrderByCreatedAtAsc(recruitId)
                .stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public RecruitCommentResponse createComment(String recruitId, RecruitCommentRequest request, String userId) {
        RecruitPost recruit = getRecruitEntity(recruitId);
        User user = userService.findById(userId);
        String name = displayName(user);
        RecruitComment comment = RecruitComment.builder()
                .recruitPost(recruit)
                .author(name)
                .authorInitials(name.substring(0, 1))
                .authorTone("mint")
                .content(request.content())
                .build();
        RecruitComment savedComment = recruitCommentRepository.save(comment);
        notificationService.notifyRecruitCommentCreated(recruit, user);
        return toCommentResponse(savedComment);
    }

    @Transactional
    public RecruitApplicationResponse apply(String recruitId, RecruitApplicationRequest request, String userId) {
        RecruitPost recruit = getRecruitEntity(recruitId);
        User user = userService.findById(userId);
        RecruitApplication existingApplication = recruitApplicationRepository
                .findFirstByRecruitPost_IdAndUser_IdOrderBySubmittedAtDesc(recruitId, user.getId())
                .orElse(null);
        if (existingApplication != null) {
            existingApplication.update(request.role(), request.body());
            return toApplicationResponse(existingApplication);
        }

        RecruitApplication application = RecruitApplication.builder()
                .recruitPost(recruit)
                .user(user)
                .role(request.role())
                .body(request.body())
                .submittedAt(LocalDateTime.now())
                .status("submitted")
                .build();
        return toApplicationResponse(recruitApplicationRepository.save(application));
    }

    public List<RecruitApplicationResponse> getMyApplications(String userId) {
        User user = userService.findById(userId);
        Set<String> seenRecruitIds = new HashSet<>();
        return recruitApplicationRepository.findByUser_IdOrderBySubmittedAtDesc(user.getId())
                .stream()
                .filter(application -> seenRecruitIds.add(application.getRecruitPost().getId()))
                .map(this::toApplicationResponse)
                .toList();
    }

    public List<RecruitApplicationResponse> getRecruitApplications(User actor, String recruitId) {
        permissionService.assertModerator(actor);
        return recruitApplicationRepository.findByRecruitPost_IdOrderBySubmittedAtDesc(recruitId)
                .stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public RecruitPostResponse bookmark(String recruitId, String userId) {
        RecruitPost recruit = getRecruitEntity(recruitId);
        User user = userService.findById(userId);
        recruitBookmarkRepository.findByRecruitPost_IdAndUser_Id(recruitId, user.getId())
                .orElseGet(() -> {
                    RecruitBookmark bookmark = RecruitBookmark.builder()
                            .recruitPost(recruit)
                            .user(user)
                            .build();
                    recruit.increaseBookmarks();
                    return recruitBookmarkRepository.save(bookmark);
                });
        if ("closing-soon".equalsIgnoreCase(recruit.getStatus())) {
            notificationService.notifyRecruitClosingSoon(user, recruit);
        }
        return toPostResponse(recruit);
    }

    private RecruitPost getRecruitEntity(String recruitId) {
        return recruitPostRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private boolean matches(RecruitPost recruit, String query) {
        return (recruit.getTitle() + " " + recruit.getShortDesc() + " "
                + String.join(" ", recruit.getTags()) + " " + String.join(" ", recruit.getTechStack()))
                .toLowerCase()
                .contains(query);
    }

    private RecruitPostResponse toPostResponse(RecruitPost recruit) {
        List<RecruitCommentResponse> comments = recruitCommentRepository.findByRecruitPost_IdOrderByCreatedAtAsc(recruit.getId())
                .stream()
                .map(this::toCommentResponse)
                .toList();
        return new RecruitPostResponse(
                recruit.getId(),
                recruit.getTitle(),
                recruit.getShortDesc(),
                recruit.getCategory(),
                recruit.getStatus(),
                recruit.getCurrentMembers(),
                recruit.getMaxMembers(),
                recruit.getHost(),
                recruit.getAuthor() == null ? null : recruit.getAuthor().getId(),
                recruit.getAuthor() == null ? recruit.getHost() : displayName(recruit.getAuthor()),
                recruit.getHostInitials(),
                recruit.getHostTone(),
                recruit.getHostRole(),
                recruit.getTrustScore(),
                recruit.getTags(),
                recruit.getTechStack(),
                recruit.getRoles().stream()
                        .map(role -> new RecruitRoleResponse(role.getLabel(), role.getCurrent(), role.getMax()))
                        .toList(),
                recruit.getMeetingType(),
                recruit.getExpectedDuration(),
                recruit.getDetailContent(),
                recruit.getProcessList(),
                comments,
                recruit.getCreatedAt() == null ? "" : recruit.getCreatedAt().format(DATE_FORMAT),
                recruit.getViews(),
                recruit.getBookmarks()
        );
    }

    private RecruitCommentResponse toCommentResponse(RecruitComment comment) {
        return new RecruitCommentResponse(
                String.valueOf(comment.getId()),
                comment.getAuthor(),
                comment.getAuthorInitials(),
                comment.getAuthorTone(),
                toTimeLabel(comment.getCreatedAt()),
                comment.getContent(),
                comment.getCreatedAt() == null ? null : comment.getCreatedAt().toString()
        );
    }

    private RecruitApplicationResponse toApplicationResponse(RecruitApplication application) {
        return new RecruitApplicationResponse(
                application.getId(),
                application.getRecruitPost().getId(),
                application.getRecruitPost().getTitle(),
                application.getRole(),
                application.getBody(),
                application.getSubmittedAt().toString(),
                application.getStatus()
        );
    }

    private String toTimeLabel(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "방금 전";
        }
        long minutes = Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        return (hours / 24) + "일 전";
    }

    private String displayName(User user) {
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getName();
    }

    @Transactional
    public void deleteRecruit(User actor, String recruitId) {
        RecruitPost recruit = getRecruitEntity(recruitId);
        assertCanManageRecruit(actor, recruit);
        recruitApplicationRepository.deleteByRecruitPost_Id(recruitId);
        recruitCommentRepository.deleteByRecruitPost_Id(recruitId);
        recruitBookmarkRepository.deleteByRecruitPost_Id(recruitId);
        recruitPostRepository.delete(recruit);
    }

    private void assertCanManageRecruit(User actor, RecruitPost recruit) {
        boolean isAuthor = actor != null
                && recruit.getAuthor() != null
                && actor.getId().equals(recruit.getAuthor().getId());
        if (!isAuthor && !permissionService.canModerate(actor)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of("#모집");
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                .toList();
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private String normalizeStatus(String status, String fallback) {
        if (!StringUtils.hasText(status)) {
            return fallback;
        }
        String normalized = status.trim().toLowerCase();
        if (Set.of("open", "closing-soon", "closed").contains(normalized)) {
            return normalized;
        }
        return fallback;
    }

    private String generateRecruitId(String title) {
        String base = title == null ? "recruit" : title.trim().toLowerCase()
                .replaceAll("[^a-z0-9가-힣]+", "-")
                .replaceAll("^-+|-+$", "");
        String candidate = base.isBlank() ? "recruit" : base;
        if (!recruitPostRepository.existsById(candidate)) {
            return candidate;
        }
        return candidate + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
