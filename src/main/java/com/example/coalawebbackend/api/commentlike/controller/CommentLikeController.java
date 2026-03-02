package com.example.coalawebbackend.api.commentlike.controller;

import com.example.coalawebbackend.api.commentlike.dto.CommentLikeResponse;
import com.example.coalawebbackend.api.commentlike.facade.CommentLikeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments/{commentId}/likes")
public class CommentLikeController implements CommentLikeControllerSpec {

    private final CommentLikeFacade commentLikeFacade;

    @PostMapping
    public ResponseEntity<CommentLikeResponse> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(commentLikeFacade.toggleLike(postId, commentId, userId));
    }
}
