package com.example.coalawebbackend.api.moderation.service;

import com.example.coalawebbackend.api.moderation.dto.CreateReportRequest;
import com.example.coalawebbackend.api.moderation.dto.ReportResponse;
import com.example.coalawebbackend.api.moderation.dto.UserSanctionRequest;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.service.CommentService;
import com.example.coalawebbackend.domain.moderation.entity.AdminActionType;
import com.example.coalawebbackend.domain.moderation.entity.CommentHistory;
import com.example.coalawebbackend.domain.moderation.entity.ContentHistoryAction;
import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import com.example.coalawebbackend.domain.moderation.entity.PostHistory;
import com.example.coalawebbackend.domain.moderation.entity.Report;
import com.example.coalawebbackend.domain.moderation.entity.ReportStatus;
import com.example.coalawebbackend.domain.moderation.entity.UserSanction;
import com.example.coalawebbackend.domain.moderation.repository.CommentHistoryRepository;
import com.example.coalawebbackend.domain.moderation.repository.PostHistoryRepository;
import com.example.coalawebbackend.domain.moderation.repository.ReportRepository;
import com.example.coalawebbackend.domain.moderation.repository.UserSanctionRepository;
import com.example.coalawebbackend.domain.moderation.service.AdminAuditService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationService {

    private static final int AUTO_HIDE_REPORT_THRESHOLD = 5;

    private final ReportRepository reportRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final PostHistoryRepository postHistoryRepository;
    private final CommentHistoryRepository commentHistoryRepository;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final PermissionService permissionService;
    private final AdminAuditService adminAuditService;

    @Transactional
    public ReportResponse createReport(User reporter, CreateReportRequest request) {
        validateReportTarget(request.targetType(), request.targetId());
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(
                reporter, request.targetType(), request.targetId())) {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }

        Report report = reportRepository.save(Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reasonType(request.reasonType())
                .reasonDetail(request.reasonDetail())
                .build());

        applyAutoHideIfNeeded(reporter, report);
        return ReportResponse.from(report);
    }

    public List<ReportResponse> getReports(User admin, ReportStatus status) {
        permissionService.assertModerator(admin);
        return reportRepository.findByStatusOrderByCreatedAtAsc(status)
                .stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional
    public ReportResponse handleReport(
            User admin,
            Long reportId,
            ReportStatus status,
            String reason,
            HttpServletRequest request
    ) {
        permissionService.assertModerator(admin);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        report.handle(admin, status);
        adminAuditService.log(admin, ModerationTargetType.POST, reportId,
                AdminActionType.HANDLE_REPORT, reason, request);
        return ReportResponse.from(report);
    }

    @Transactional
    public void hidePost(User admin, Long postId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Post post = postService.getPostById(postId);
        post.hide();
        savePostHistory(post, admin, ContentHistoryAction.ADMIN_HIDDEN, reason);
        adminAuditService.log(admin, ModerationTargetType.POST, postId, AdminActionType.HIDE_POST, reason, request);
    }

    @Transactional
    public void restorePost(User admin, Long postId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Post post = postService.getPostById(postId);
        post.restore();
        savePostHistory(post, admin, ContentHistoryAction.ADMIN_RESTORED, reason);
        adminAuditService.log(admin, ModerationTargetType.POST, postId, AdminActionType.RESTORE_POST, reason, request);
    }

    @Transactional
    public void adminDeletePost(User admin, Long postId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Post post = postService.getPostById(postId);
        savePostHistory(post, admin, ContentHistoryAction.ADMIN_DELETED, reason);
        post.softDelete(admin, reason, true);
        adminAuditService.log(admin, ModerationTargetType.POST, postId, AdminActionType.DELETE_POST, reason, request);
    }

    @Transactional
    public void lockPost(User admin, Long postId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Post post = postService.getPostById(postId);
        post.lock();
        savePostHistory(post, admin, ContentHistoryAction.UPDATED, reason);
        adminAuditService.log(admin, ModerationTargetType.POST, postId, AdminActionType.LOCK_POST, reason, request);
    }

    @Transactional
    public void unlockPost(User admin, Long postId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Post post = postService.getPostById(postId);
        post.unlock();
        savePostHistory(post, admin, ContentHistoryAction.UPDATED, reason);
        adminAuditService.log(admin, ModerationTargetType.POST, postId, AdminActionType.UNLOCK_POST, reason, request);
    }

    @Transactional
    public void hideComment(User admin, Long commentId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Comment comment = commentService.getComment(commentId);
        comment.hide();
        saveCommentHistory(comment, admin, ContentHistoryAction.ADMIN_HIDDEN, reason);
        adminAuditService.log(admin, ModerationTargetType.COMMENT, commentId,
                AdminActionType.HIDE_COMMENT, reason, request);
    }

    @Transactional
    public void restoreComment(User admin, Long commentId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Comment comment = commentService.getComment(commentId);
        comment.restore();
        saveCommentHistory(comment, admin, ContentHistoryAction.ADMIN_RESTORED, reason);
        adminAuditService.log(admin, ModerationTargetType.COMMENT, commentId,
                AdminActionType.RESTORE_COMMENT, reason, request);
    }

    @Transactional
    public void adminDeleteComment(User admin, Long commentId, String reason, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        Comment comment = commentService.getComment(commentId);
        saveCommentHistory(comment, admin, ContentHistoryAction.ADMIN_DELETED, reason);
        comment.softDelete(admin, reason, true);
        adminAuditService.log(admin, ModerationTargetType.COMMENT, commentId,
                AdminActionType.DELETE_COMMENT, reason, request);
    }

    @Transactional
    public void sanctionUser(User admin, UserSanctionRequest sanctionRequest, HttpServletRequest request) {
        permissionService.assertModerator(admin);
        User target = userService.findById(String.valueOf(sanctionRequest.userId()));
        if (target.getRole().canModerate() && !admin.getRole().atLeast(UserRole.SUPER_ADMIN)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        LocalDateTime startAt = sanctionRequest.startAt() == null ? LocalDateTime.now() : sanctionRequest.startAt();
        userSanctionRepository.save(UserSanction.builder()
                .user(target)
                .type(sanctionRequest.type())
                .reason(sanctionRequest.reason())
                .startAt(startAt)
                .endAt(sanctionRequest.endAt())
                .createdBy(admin)
                .build());
        adminAuditService.log(admin, ModerationTargetType.USER, target.getId(),
                AdminActionType.SANCTION_USER, sanctionRequest.reason(), request);
    }

    private void validateReportTarget(ModerationTargetType targetType, Long targetId) {
        if (targetType == ModerationTargetType.POST) {
            postService.getVisiblePostById(targetId);
            return;
        }
        if (targetType == ModerationTargetType.COMMENT) {
            Comment comment = commentService.getComment(targetId);
            if (!comment.isVisible() || !comment.getPost().isVisible()) {
                throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
            }
            return;
        }
        throw new CustomException(ErrorCode.VALIDATION_FAILED);
    }

    private void applyAutoHideIfNeeded(User reporter, Report report) {
        long count = reportRepository.countDistinctReporterByTargetTypeAndTargetIdAndStatusIn(
                report.getTargetType(),
                report.getTargetId(),
                List.of(ReportStatus.PENDING, ReportStatus.AUTO_HIDDEN));
        if (count < AUTO_HIDE_REPORT_THRESHOLD) {
            return;
        }

        report.markAutoHidden();
        if (report.getTargetType() == ModerationTargetType.POST) {
            Post post = postService.getPostById(report.getTargetId());
            post.hide();
            savePostHistory(post, reporter, ContentHistoryAction.AUTO_HIDDEN, "신고 누적 자동 숨김");
        } else if (report.getTargetType() == ModerationTargetType.COMMENT) {
            Comment comment = commentService.getComment(report.getTargetId());
            comment.hide();
            saveCommentHistory(comment, reporter, ContentHistoryAction.AUTO_HIDDEN, "신고 누적 자동 숨김");
        }
    }

    private void savePostHistory(Post post, User actor, ContentHistoryAction action, String reason) {
        postHistoryRepository.save(PostHistory.builder()
                .post(post)
                .actor(actor)
                .action(action)
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .reason(reason)
                .build());
    }

    private void saveCommentHistory(Comment comment, User actor, ContentHistoryAction action, String reason) {
        commentHistoryRepository.save(CommentHistory.builder()
                .comment(comment)
                .actor(actor)
                .action(action)
                .content(comment.getContent())
                .status(comment.getStatus())
                .reason(reason)
                .build());
    }
}
