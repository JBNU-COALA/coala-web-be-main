package com.example.coalawebbackend.api.postlike.facade;

import com.example.coalawebbackend.api.postlike.dto.PostLikeResponse;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.postlike.service.PostLikeService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeFacade {

    private final PostLikeService postLikeService;
    private final PostService postService;
    private final UserService userService;

    public PostLikeResponse toggleLike(Long postId, String userId) {
        Post post = postService.getVisiblePostById(postId);
        User user = userService.findById(userId);
        return postLikeService.toggleLike(user, post);
    }
}
