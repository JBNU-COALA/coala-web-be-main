package com.example.coalawebbackend.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.comment.service.CommentService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;


    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_success() {
        // given
        Post post = mock(Post.class);
        User user = mock(User.class);
        CreateCommentRequest request = mock(CreateCommentRequest.class);
        Comment comment = Comment.create(post, user, "테스트 댓글");

        given(request.getContent()).willReturn("테스트 댓글");
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CreateCommentResponse response = commentService.createComment(post, user, request);

        // then
        assertThat(response).isNotNull();
        then(commentRepository).should(times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_success() {
        // given
        Long postId = 1L;
        Post post = mock(Post.class);
        User user = mock(User.class);
        Comment comment = Comment.create(post, user, "테스트 댓글");

        given(commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId))
                .willReturn(List.of(comment));

        // when
        List<CommentResponse> responses = commentService.getComments(postId);

        // then
        assertThat(responses).hasSize(1);
        then(commentRepository).should(times(1)).findByPost_PostIdOrderByCreatedAtAsc(postId);
    }

    @Test
    @DisplayName("댓글이 없으면 빈 리스트 반환")
    void getComments_empty() {
        // given
        Long postId = 1L;
        given(commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId))
                .willReturn(List.of());

        // when
        List<CommentResponse> responses = commentService.getComments(postId);

        // then
        assertThat(responses).isEmpty();
    }


    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        // given
        Long postId = 1L;
        Long commentId = 1L;

        Post post = mock(Post.class);
        User user = mock(User.class);
        Comment comment = Comment.create(post, user, "기존 댓글");
        UpdateCommentRequest request = mock(UpdateCommentRequest.class);

        given(post.getPostId()).willReturn(postId);
        given(user.getId()).willReturn(1L);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(request.getContent()).willReturn("수정된 댓글");

        // when
        UpdateCommentResponse response = commentService.updateComment(postId, commentId, request, user);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글 없음")
    void updateComment_commentNotFound() {
        // given
        Long postId = 1L;
        Long commentId = 999L;
        User user = mock(User.class);
        UpdateCommentRequest request = mock(UpdateCommentRequest.class);

        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(postId, commentId, request, user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("댓글 수정 실패 - 다른 게시글의 댓글")
    void updateComment_commentNotBelongsToPost() {
        // given
        Long postId = 1L;
        Long otherPostId = 2L;
        Long commentId = 1L;

        Post post = mock(Post.class);
        User user = mock(User.class);
        Comment comment = Comment.create(post, user, "테스트 댓글");
        UpdateCommentRequest request = mock(UpdateCommentRequest.class);

        given(post.getPostId()).willReturn(otherPostId);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(postId, commentId, request, user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 불일치")
    void updateComment_accessDenied() {
        // given
        Long postId = 1L;
        Long commentId = 1L;

        Post post = mock(Post.class);
        User owner = mock(User.class);
        User other = mock(User.class);
        Comment comment = Comment.create(post, owner, "테스트 댓글");
        UpdateCommentRequest request = mock(UpdateCommentRequest.class);

        given(post.getPostId()).willReturn(postId);
        given(owner.getId()).willReturn(1L);
        given(other.getId()).willReturn(2L);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(postId, commentId, request, other))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
                });
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() {
        // given
        Long postId = 1L;
        Long commentId = 1L;

        Post post = mock(Post.class);
        User user = mock(User.class);
        Comment comment = Comment.create(post, user, "테스트 댓글");

        given(post.getPostId()).willReturn(postId);
        given(user.getId()).willReturn(1L);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(postId, commentId, user);

        // then
        then(commentRepository).should(times(1)).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 없음")
    void deleteComment_commentNotFound() {
        // given
        Long postId = 1L;
        Long commentId = 999L;
        User user = mock(User.class);

        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentService.deleteComment(postId, commentId, user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자 불일치")
    void deleteComment_accessDenied() {
        // given
        Long postId = 1L;
        Long commentId = 1L;

        Post post = mock(Post.class);
        User owner = mock(User.class);
        User other = mock(User.class);
        Comment comment = Comment.create(post, owner, "테스트 댓글");

        given(post.getPostId()).willReturn(postId);
        given(owner.getId()).willReturn(1L);
        given(other.getId()).willReturn(2L);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.deleteComment(postId, commentId, other))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
                });
    }
}
