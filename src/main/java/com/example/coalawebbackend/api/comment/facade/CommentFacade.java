package com.example.coalawebbackend.api.comment.facade;

import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import com.example.coalawebbackend.domain.comment.service.CommentService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentFacade {

    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;

    public CreateCommentResponse createComment(Long postId, CreateCommentRequest request, String userId) {
        Post post = postService.getVisiblePostById(postId);
        User user = userService.findById(userId);
        return commentService.createComment(post, user, request);
    }

    public List<CommentResponse> getComments(Long postId) {
        postService.getVisiblePostById(postId);
        return commentService.getComments(postId);
    }

    public List<CommentResponse> getReplies(Long postId, Long commentId) {
        postService.getVisiblePostById(postId);
        commentService.getCommentInPost(postId, commentId);
        return commentService.getReplies(postId, commentId);
    }

    public CreateCommentResponse createReply(Long postId, Long commentId, CreateCommentRequest request, String userId) {
        Post post = postService.getVisiblePostById(postId);
        User user = userService.findById(userId);
        return commentService.createComment(post, user, new CreateCommentRequest(request.getContent(), commentId));
    }

    public UpdateCommentResponse updateComment(Long postId, Long commentId, UpdateCommentRequest request, String userId) {
        postService.getPostById(postId);
        User user = userService.findById(userId);
        return commentService.updateComment(postId, commentId, request, user);
    }

    public void deleteComment(Long postId, Long commentId, String userId) {
        postService.getPostById(postId);
        User user = userService.findById(userId);
        commentService.deleteComment(postId, commentId, user);
    }
}
