package com.example.coalawebbackend.postlike;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.example.coalawebbackend.api.notification.service.NotificationService;
import com.example.coalawebbackend.api.postlike.dto.PostLikeResponse;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.postlike.entity.PostLike;
import com.example.coalawebbackend.domain.postlike.repository.PostLikeRepository;
import com.example.coalawebbackend.domain.postlike.service.PostLikeService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @InjectMocks
    private PostLikeService postLikeService;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("좋아요 추가 성공 - 기존 좋아요 없음")
    void toggleLike_add() {
        // given
        User user = mock(User.class);
        Post post = mock(Post.class);

        given(postLikeRepository.findByUserAndPostWithLock(user, post))
                .willReturn(Optional.empty());
        given(postLikeRepository.countByPost(post)).willReturn(1L);

        // when
        PostLikeResponse response = postLikeService.toggleLike(user, post);

        // then
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1L);
        then(postLikeRepository).should(times(1)).saveAndFlush(any(PostLike.class));
        then(notificationService).should(times(1)).notifyPostLiked(post, user);
    }

    @Test
    @DisplayName("좋아요 취소 성공 - 기존 좋아요 있음")
    void toggleLike_cancel() {
        // given
        User user = mock(User.class);
        Post post = mock(Post.class);
        PostLike postLike = mock(PostLike.class);

        given(postLikeRepository.findByUserAndPostWithLock(user, post))
                .willReturn(Optional.of(postLike));
        given(postLikeRepository.countByPost(post)).willReturn(0L);

        // when
        PostLikeResponse response = postLikeService.toggleLike(user, post);

        // then
        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikeCount()).isEqualTo(0L);
        then(postLikeRepository).should(times(1)).delete(postLike);
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("좋아요 추가 후 likeCount 정확성 확인")
    void toggleLike_likeCount() {
        // given
        User user = mock(User.class);
        Post post = mock(Post.class);

        given(postLikeRepository.findByUserAndPostWithLock(user, post))
                .willReturn(Optional.empty());
        given(postLikeRepository.countByPost(post)).willReturn(3L);

        // when
        PostLikeResponse response = postLikeService.toggleLike(user, post);

        // then
        assertThat(response.getLikeCount()).isEqualTo(3L);
    }
}
