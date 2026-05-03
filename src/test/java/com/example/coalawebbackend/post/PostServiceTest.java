package com.example.coalawebbackend.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.example.coalawebbackend.api.post.dto.CreatePostResponse;
import com.example.coalawebbackend.api.post.dto.PostDetailResponse;
import com.example.coalawebbackend.api.post.dto.PostListResponse;
import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.api.post.dto.UpdatePostResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.service.BoardService;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.postlike.repository.PostLikeRepository;
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
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private BoardService boardService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() {
        // given
        User user = mock(User.class);
        Board board = mock(Board.class);
        Post post = mock(Post.class);
        PostRequest request = mock(PostRequest.class);

        given(boardService.getBoardById(1L)).willReturn(board);
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        CreatePostResponse response = postService.createPost(user, 1L, request);

        // then
        assertThat(response).isNotNull();
        then(boardService).should(times(1)).getBoardById(1L);
        then(postRepository).should(times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getPosts_success() {
        // given
        Board board = mock(Board.class);
        User user = mock(User.class);
        Post post = mock(Post.class);

        given(post.getBoard()).willReturn(board);
        given(post.getUser()).willReturn(user);
        given(post.getPostId()).willReturn(1L);
        given(postRepository.findByBoardBoardId(1L)).willReturn(List.of(post));
        given(commentRepository.countByPost_PostId(1L)).willReturn(2L);
        given(postLikeRepository.countByPost(post)).willReturn(3L);

        // when
        List<PostListResponse> result = postService.getPosts(1L);

        // then
        assertThat(result).hasSize(1);
        then(postRepository).should(times(1)).findByBoardBoardId(1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostDetail_success() {
        // given
        Long boardId = 1L;
        Long postId = 1L;

        Board board = mock(Board.class);
        User user = mock(User.class);
        Post post = mock(Post.class);

        given(board.getBoardId()).willReturn(boardId);
        given(post.getPostId()).willReturn(postId);
        given(post.getBoard()).willReturn(board);
        given(post.getUser()).willReturn(user);
        given(commentRepository.countByPost_PostId(postId)).willReturn(2L);
        given(postLikeRepository.countByPost(post)).willReturn(3L);

        // when
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        PostDetailResponse response = postService.getPostDetail(boardId, postId);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("게시글 조회 실패 - 게시글 없음")
    void getPostById_notFound() {
        // given
        Long postId = 999L;

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
                });
    }


    @Test
    @DisplayName("게시글 상세 조회 실패 - 다른 게시판의 게시글")
    void getPostDetail_postNotBelongsToBoard() {
        Long boardId = 1L;
        Long otherBoardId = 2L;
        Long postId = 1L;

        Board board = mock(Board.class);
        Post post = mock(Post.class);

        given(board.getBoardId()).willReturn(otherBoardId);
        given(post.getBoard()).willReturn(board);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.getPostDetail(boardId, postId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ex = (CustomException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() {
        // given
        Long postId = 1L;
        User user = mock(User.class);
        User postOwner = mock(User.class);
        Post post = mock(Post.class);
        PostRequest request = mock(PostRequest.class);

        given(user.getId()).willReturn(1L);
        given(postOwner.getId()).willReturn(1L);
        given(post.getUser()).willReturn(postOwner);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        UpdatePostResponse response = postService.updatePost(postId, request, user);

        // then
        assertThat(response).isNotNull();
        then(post).should(times(1)).update(request.getTitle(), request.getContent());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void updatePost_fail_postNotFound() {
        // given
        User user = mock(User.class);
        PostRequest request = mock(PostRequest.class);

        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, request, user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.POST_NOT_FOUND));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자 불일치")
    void updatePost_fail_accessDenied() {
        // given
        Long postId = 1L;
        User owner = mock(User.class);
        User other = mock(User.class);
        Post post = mock(Post.class);
        PostRequest request = mock(PostRequest.class);

        given(owner.getId()).willReturn(1L);
        given(other.getId()).willReturn(2L);
        given(post.getUser()).willReturn(owner);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, request, other))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        // given
        Long postId = 1L;
        User user = mock(User.class);
        User postOwner = mock(User.class);
        Post post = mock(Post.class);

        given(user.getId()).willReturn(1L);
        given(postOwner.getId()).willReturn(1L);
        given(post.getUser()).willReturn(postOwner);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        postService.deletePost(postId, user);

        // then
        then(postRepository).should(times(1)).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자 불일치")
    void deletePost_fail_accessDenied() {
        // given
        Long postId = 1L;
        User owner = mock(User.class);
        User other = mock(User.class);
        Post post = mock(Post.class);

        given(owner.getId()).willReturn(1L);
        given(other.getId()).willReturn(2L);
        given(post.getUser()).willReturn(owner);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(postId, other))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
