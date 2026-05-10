package com.example.coalawebbackend.api.commentlike.facade;

import com.example.coalawebbackend.api.commentlike.dto.CommentLikeResponse;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.service.CommentService;
import com.example.coalawebbackend.domain.commentlike.service.CommentLikeService;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentLikeFacade {

    private final CommentLikeService commentLikeService;
    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;

    public CommentLikeResponse toggleLike(Long postId, Long commentId, String userId) {
        postService.getVisiblePostById(postId);
        Comment comment = commentService.getCommentInPost(postId, commentId);
        if (!comment.isVisible()) {
            throw new com.example.coalawebbackend.common.exception.CustomException(
                    com.example.coalawebbackend.common.enums.ErrorCode.COMMENT_NOT_FOUND);
        }
        User user = userService.findById(userId);
        return commentLikeService.toggleLike(user, comment);
    }
}
