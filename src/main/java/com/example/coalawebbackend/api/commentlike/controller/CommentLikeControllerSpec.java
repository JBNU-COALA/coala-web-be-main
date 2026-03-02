package com.example.coalawebbackend.api.commentlike.controller;

import com.example.coalawebbackend.api.commentlike.dto.CommentLikeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "CommentLike API", description = "댓글 좋아요 관련 API")
public interface CommentLikeControllerSpec {

    @Operation(summary = "댓글 좋아요 토글", description = "좋아요가 없으면 추가, 있으면 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공",
                    content = @Content(schema = @Schema(implementation = CommentLikeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    ResponseEntity<CommentLikeResponse> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            String userId
    );
}
