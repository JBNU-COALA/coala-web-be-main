package com.example.coalawebbackend.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willThrow;

import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import com.example.coalawebbackend.api.notification.service.NotificationService;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.anonymous.service.AnonymousProfileService;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.entity.CommentStatus;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.comment.service.CommentService;
import com.example.coalawebbackend.domain.commentlike.repository.CommentLikeRepository;
import com.example.coalawebbackend.domain.moderation.entity.CommentHistory;
import com.example.coalawebbackend.domain.moderation.repository.CommentHistoryRepository;
import com.example.coalawebbackend.domain.moderation.service.ContentSafetyService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
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

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentHistoryRepository commentHistoryRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private ContentSafetyService contentSafetyService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AnonymousProfileService anonymousProfileService;

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_success() {
        // given
        Post post = mock(Post.class);
        Board board = mock(Board.class);
        User user = mock(User.class);
        CreateCommentRequest request = mock(CreateCommentRequest.class);
        Comment comment = Comment.create(post, user, "테스트 댓글");

        given(post.getBoard()).willReturn(board);
        given(request.getContent()).willReturn("테스트 댓글");
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CreateCommentResponse response = commentService.createComment(post, user, request);

        // then
        assertThat(response).isNotNull();
        then(commentRepository).should(times(1)).save(any(Comment.class));
        then(notificationService).should(times(1)).notifyCommentCreated(post, comment);
    }

    @Test
    @DisplayName("답글 생성 성공 - 부모 댓글 알림 호출")
    void createReply_success_notifiesParentCommentOwner() {
        // given
        Long postId = 1L;
        Long parentCommentId = 10L;
        Post post = mock(Post.class);
        Board board = mock(Board.class);
        User user = mock(User.class);
        User parentUser = mock(User.class);
        Comment parent = Comment.create(post, parentUser, "부모 댓글");
        CreateCommentRequest request = new CreateCommentRequest("테스트 답글", parentCommentId);

        given(post.getBoard()).willReturn(board);
        given(post.getPostId()).willReturn(postId);
        given(user.getId()).willReturn(2L);
        given(user.getNickname()).willReturn(null);
        given(user.getName()).willReturn("답글 작성자");
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreateCommentResponse response = commentService.createComment(post, user, request);

        // then
        assertThat(response).isNotNull();
        then(notificationService).should(times(1)).notifyReplyCreated(eq(post), eq(parent), any(Comment.class));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_success() {
        // given
        Long postId = 1L;
        Post post = mock(Post.class);
        Board board = mock(Board.class);
        User user = mock(User.class);
        Comment comment = Comment.create(post, user, "테스트 댓글");
        List<CommentStatus> visibleStatuses = List.of(
                CommentStatus.ACTIVE,
                CommentStatus.DELETED,
                CommentStatus.ADMIN_DELETED);

        given(post.getBoard()).willReturn(board);
        given(commentRepository.findVisibleParents(postId, visibleStatuses))
                .willReturn(List.of(comment));

        // when
        List<CommentResponse> responses = commentService.getComments(postId);

        // then
        assertThat(responses).hasSize(1);
        then(commentRepository).should(times(1)).findVisibleParents(postId, visibleStatuses);
    }

    @Test
    @DisplayName("댓글이 없으면 빈 리스트 반환")
    void getComments_empty() {
        // given
        Long postId = 1L;
        List<CommentStatus> visibleStatuses = List.of(
                CommentStatus.ACTIVE,
                CommentStatus.DELETED,
                CommentStatus.ADMIN_DELETED);
        given(commentRepository.findVisibleParents(postId, visibleStatuses))
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
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(request.getContent()).willReturn("수정된 댓글");

        // when
        UpdateCommentResponse response = commentService.updateComment(postId, commentId, request, user);

        // then
        assertThat(response).isNotNull();
        then(commentHistoryRepository).should(times(1)).save(any(CommentHistory.class));
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
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(permissionService).assertCanUpdateComment(other, comment);

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
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(postId, commentId, user);

        // then
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
        then(permissionService).should(times(1)).assertCanUserDeleteComment(user, comment);
        then(commentHistoryRepository).should(times(1)).save(any(CommentHistory.class));
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
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(permissionService).assertCanUserDeleteComment(other, comment);

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
