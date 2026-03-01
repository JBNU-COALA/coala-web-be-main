package com.example.coalawebbackend.api.comment.controller;


import com.example.coalawebbackend.api.comment.dto.CommentResponse;
import com.example.coalawebbackend.api.comment.dto.CreateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.CreateCommentResponse;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentRequest;
import com.example.coalawebbackend.api.comment.dto.UpdateCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Comment API", description = "댓글 관련 API")
public interface CommentControllerSpec {

    @Operation(summary = "댓글 생성", description = "게시글에 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateCommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<CreateCommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            String userId
    );

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId
    );

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UpdateCommentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    ResponseEntity<UpdateCommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            String userId
    );

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            String userId
    );
}
