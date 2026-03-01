package com.example.coalawebbackend.api.postlike.controller;

import com.example.coalawebbackend.api.postlike.dto.PostLikeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "PostLike API", description = "게시글 좋아요 관련 API")
public interface PostLikeControllerSpec {

    @Operation(summary = "게시글 좋아요 토글", description = "좋아요가 없으면 추가, 있으면 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공",
                    content = @Content(schema = @Schema(implementation = PostLikeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<PostLikeResponse> toggleLike(
            @PathVariable Long postId,
            @Parameter(hidden = true) String userId
    );
}
