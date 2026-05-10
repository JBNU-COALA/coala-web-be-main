package com.example.coalawebbackend.domain.comment.service;

import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.entity.CommentStatus;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.commentlike.repository.CommentLikeRepository;
import com.example.coalawebbackend.domain.moderation.entity.CommentHistory;
import com.example.coalawebbackend.domain.moderation.entity.ContentHistoryAction;
import com.example.coalawebbackend.domain.moderation.repository.CommentHistoryRepository;
import com.example.coalawebbackend.domain.moderation.service.ContentSafetyService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentHistoryRepository commentHistoryRepository;
    private final PermissionService permissionService;
    private final ContentSafetyService contentSafetyService;
    private static final List<CommentStatus> PUBLIC_COMMENT_STATUSES = List.of(
            CommentStatus.ACTIVE,
            CommentStatus.DELETED,
            CommentStatus.ADMIN_DELETED);

    @Transactional
    public CreateCommentResponse createComment(Post post, User user, CreateCommentRequest request) {
        permissionService.assertCanComment(user, post);
        contentSafetyService.validateMarkdown(request.getContent());
        Comment parent = null;
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId != null && parentCommentId > 0) {
            parent = getCommentInPost(post.getPostId(), parentCommentId);
            if (!parent.isVisible()) {
                throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
            }
        }
        Comment comment = parent == null
                ? Comment.create(post, user, request.getContent())
                : Comment.createReply(post, user, parent, request.getContent());
        Comment savedComment = commentRepository.save(comment);
        saveHistory(savedComment, user, ContentHistoryAction.CREATED, null);
        return CreateCommentResponse.from(savedComment);
    }

    public List<CommentResponse> getComments(Long postId) {
        return commentRepository
                .findVisibleParents(postId, PUBLIC_COMMENT_STATUSES)
                .stream()
                .map(comment -> CommentResponse.from(comment, getReplies(postId, comment.getId())))
                .toList();
    }

    public List<CommentResponse> getReplies(Long postId, Long parentCommentId) {
        return commentRepository
                .findVisibleReplies(postId, parentCommentId, PUBLIC_COMMENT_STATUSES)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public UpdateCommentResponse updateComment(Long postId, Long commentId, UpdateCommentRequest request, User user) {
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, postId);
        permissionService.assertCanUpdateComment(user, comment);
        contentSafetyService.validateMarkdown(request.getContent());
        saveHistory(comment, user, ContentHistoryAction.UPDATED, "before update");
        comment.update(request.getContent());
        return UpdateCommentResponse.of(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, User user) {
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, postId);
        permissionService.assertCanUserDeleteComment(user, comment);
        saveHistory(comment, user, ContentHistoryAction.USER_DELETED, "user deleted");
        comment.softDelete(user, "사용자 삭제", false);
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPost().getPostId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    public Comment getCommentInPost(Long postId, Long commentId) {
        Comment comment = getComment(commentId);
        if (!comment.getPost().getPostId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }

    private void deleteLikesRecursively(Comment comment) {
        comment.getReplies().forEach(this::deleteLikesRecursively);
        commentLikeRepository.deleteByComment(comment);
    }

    private void saveHistory(Comment comment, User actor, ContentHistoryAction action, String reason) {
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
