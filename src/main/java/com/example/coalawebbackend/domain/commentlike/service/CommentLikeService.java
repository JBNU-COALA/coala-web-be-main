package com.example.coalawebbackend.domain.commentlike.service;

import com.example.coalawebbackend.api.commentlike.dto.CommentLikeResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.commentlike.entity.CommentLike;
import com.example.coalawebbackend.domain.commentlike.repository.CommentLikeRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public CommentLikeResponse toggleLike(User user, Comment comment) {
        Optional<CommentLike> existing = commentLikeRepository.findByUserAndCommentWithLock(user, comment);

        if (existing.isPresent()) {
            commentLikeRepository.delete(existing.get());
            long likeCount = commentLikeRepository.countByComment(comment);
            return CommentLikeResponse.of(false, likeCount);
        }
        try {
            commentLikeRepository.saveAndFlush(CommentLike.create(user, comment));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_LIKE);
        }
        long likeCount = commentLikeRepository.countByComment(comment);
        return CommentLikeResponse.of(true, likeCount);
    }
}
