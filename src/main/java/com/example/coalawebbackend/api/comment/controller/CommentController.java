package com.example.coalawebbackend.api.comment.controller;

import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import com.example.coalawebbackend.api.comment.facade.CommentFacade;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController implements CommentControllerSpec{

    private final CommentFacade commentFacade;  // Service → Facade

    @PostMapping
    public ResponseEntity<CreateCommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(
                commentFacade.createComment(postId, request, userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(
                commentFacade.getComments(postId)
        );
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<UpdateCommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(
                commentFacade.updateComment(postId, commentId, request, userId)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal String userId
    ) {
        commentFacade.deleteComment(postId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
