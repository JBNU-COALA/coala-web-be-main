package com.example.coalawebbackend.domain.moderation.service;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SanctionPolicyService sanctionPolicyService;

    public void assertCanCreatePost(User user, Board board) {
        sanctionPolicyService.assertCanWritePost(user);
        if (isNoticeBoard(board) && !canModerate(user)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCanComment(User user, Post post) {
        sanctionPolicyService.assertCanWriteComment(user);
        if (!post.isVisible() || post.isLocked()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (isNoticeBoard(post.getBoard()) && !canModerate(user)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCanUpdatePost(User user, Post post) {
        if (!post.isMutable()) {
            throw new CustomException(ErrorCode.POST_NOT_EDITABLE);
        }
        if (!isOwner(user, post.getUser())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCanUserDeletePost(User user, Post post) {
        if (!post.isMutable()) {
            throw new CustomException(ErrorCode.POST_NOT_DELETABLE);
        }
        if (!isOwner(user, post.getUser())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCanUpdateComment(User user, Comment comment) {
        if (!comment.isMutable() || !comment.getPost().isVisible()) {
            throw new CustomException(ErrorCode.COMMENT_NOT_EDITABLE);
        }
        if (!isOwner(user, comment.getUser())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCanUserDeleteComment(User user, Comment comment) {
        if (!comment.isMutable()) {
            throw new CustomException(ErrorCode.COMMENT_NOT_EDITABLE);
        }
        if (!isOwner(user, comment.getUser())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertModerator(User user) {
        if (!canModerate(user)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public boolean canModerate(User user) {
        return user != null && user.getRole().canModerate();
    }

    private boolean isOwner(User actor, User owner) {
        return actor != null && owner != null && actor.getId().equals(owner.getId());
    }

    private boolean isNoticeBoard(Board board) {
        return board != null && "공지".equals(board.getName());
    }
}
