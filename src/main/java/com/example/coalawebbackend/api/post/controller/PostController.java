package com.example.coalawebbackend.api.post.controller;

import com.example.coalawebbackend.api.post.dto.CreatePostResponse;
import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.api.post.dto.PostResponse;
import com.example.coalawebbackend.api.post.dto.UpdatePostResponse;
import com.example.coalawebbackend.api.post.facade.PostFacade;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController implements PostControllerSpec {

    private final PostFacade postFacade;

    @PostMapping("/boards/{boardId}/posts")
    public ResponseEntity<CreatePostResponse> createPost(
            @PathVariable Long boardId,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postFacade.createPost(userId, boardId, request));
    }


    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(
            @PathVariable Long boardId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postFacade.getPosts(boardId));
    }

    @GetMapping("/boards/{boardId}/posts/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(
            @PathVariable Long boardId,
            @PathVariable Long postId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postFacade.getPostDetail(boardId, postId));
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postFacade.updatePost(postId, request, userId));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId
    ) {
        postFacade.deletePost(postId, userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
