package com.example.coalawebbackend.commentlike;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.example.coalawebbackend.api.commentlike.dto.CommentLikeResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.commentlike.entity.CommentLike;
import com.example.coalawebbackend.domain.commentlike.repository.CommentLikeRepository;
import com.example.coalawebbackend.domain.commentlike.service.CommentLikeService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

    @InjectMocks
    private CommentLikeService commentLikeService;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Test
    @DisplayName("댓글 좋아요 추가 성공 - 기존 좋아요 없음")
    void toggleLike_add() {
        // given
        User user = mock(User.class);
        Comment comment = mock(Comment.class);

        given(commentLikeRepository.findByUserAndCommentWithLock(user, comment))
                .willReturn(Optional.empty());
        given(commentLikeRepository.countByComment(comment)).willReturn(1L);

        // when
        CommentLikeResponse response = commentLikeService.toggleLike(user, comment);

        // then
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1L);
        then(commentLikeRepository).should(times(1)).saveAndFlush(any(CommentLike.class));
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공 - 기존 좋아요 있음")
    void toggleLike_cancel() {
        // given
        User user = mock(User.class);
        Comment comment = mock(Comment.class);
        CommentLike commentLike = mock(CommentLike.class);

        given(commentLikeRepository.findByUserAndCommentWithLock(user, comment))
                .willReturn(Optional.of(commentLike));
        given(commentLikeRepository.countByComment(comment)).willReturn(0L);

        // when
        CommentLikeResponse response = commentLikeService.toggleLike(user, comment);

        // then
        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikeCount()).isEqualTo(0L);
        then(commentLikeRepository).should(times(1)).delete(commentLike);
    }

    @Test
    @DisplayName("댓글 좋아요 추가 실패 - 중복 좋아요")
    void toggleLike_duplicate() {
        // given
        User user = mock(User.class);
        Comment comment = mock(Comment.class);

        given(commentLikeRepository.findByUserAndCommentWithLock(user, comment))
                .willReturn(Optional.empty());
        given(commentLikeRepository.saveAndFlush(any(CommentLike.class)))
                .willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> commentLikeService.toggleLike(user, comment))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_LIKE);
                });
    }

    @Test
    @DisplayName("댓글 좋아요 추가 후 likeCount 정확성 확인")
    void toggleLike_likeCount() {
        // given
        User user = mock(User.class);
        Comment comment = mock(Comment.class);

        given(commentLikeRepository.findByUserAndCommentWithLock(user, comment))
                .willReturn(Optional.empty());
        given(commentLikeRepository.countByComment(comment)).willReturn(3L);

        // when
        CommentLikeResponse response = commentLikeService.toggleLike(user, comment);

        // then
        assertThat(response.getLikeCount()).isEqualTo(3L);
    }
}
