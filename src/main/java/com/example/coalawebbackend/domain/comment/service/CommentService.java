package com.example.coalawebbackend.domain.comment.service;

import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
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

    @Transactional
    public CreateCommentResponse createComment(Post post, User user, CreateCommentRequest request) {
        Comment comment = Comment.create(post, user, request.getContent());
        return CreateCommentResponse.from(commentRepository.save(comment));
    }

    public List<CommentResponse> getComments(Long postId) {
        return commentRepository
                .findByPost_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public UpdateCommentResponse updateComment(Long postId, Long commentId, UpdateCommentRequest request, User user) {
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, postId);
        validateCommentOwner(comment, user);
        comment.update(request.getContent());
        return UpdateCommentResponse.of(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, User user) {
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, postId);
        validateCommentOwner(comment, user);
        commentRepository.delete(comment);
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

    private void validateCommentOwner(Comment comment, User user) {

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
