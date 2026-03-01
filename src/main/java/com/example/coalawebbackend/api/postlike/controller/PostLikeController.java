package com.example.coalawebbackend.api.postlike.controller;

import com.example.coalawebbackend.api.postlike.dto.PostLikeResponse;
import com.example.coalawebbackend.api.postlike.facade.PostLikeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class PostLikeController implements PostLikeControllerSpec {

    private final PostLikeFacade postLikeFacade;

    @PostMapping
    public ResponseEntity<PostLikeResponse> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(postLikeFacade.toggleLike(postId, userId));
    }
}
