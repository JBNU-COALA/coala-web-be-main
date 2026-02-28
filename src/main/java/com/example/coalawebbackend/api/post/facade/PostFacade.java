package com.example.coalawebbackend.api.post.facade;

import com.example.coalawebbackend.api.post.dto.CreatePostResponse;
import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.api.post.dto.PostResponse;
import com.example.coalawebbackend.api.post.dto.UpdatePostResponse;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFacade {

    private final UserService userService;
    private final PostService postService;

    @Transactional
    public CreatePostResponse createPost(String userId, Long boardId, PostRequest request) {
        User user = userService.findById(userId);
        return postService.createPost(user, boardId, request);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId, PostRequest request, String userId) {
        User user = userService.findById(userId);
        return postService.updatePost(postId, request, user);
    }

    @Transactional
    public void deletePost(Long postId, String userId) {
        User user = userService.findById(userId);
        postService.deletePost(postId, user);
    }

    public List<PostResponse> getPosts(Long boardId) {
        return postService.getPosts(boardId);
    }

    public PostResponse getPostDetail(Long boardId, Long postId) {
        return postService.getPostDetail(boardId, postId);
    }
}
