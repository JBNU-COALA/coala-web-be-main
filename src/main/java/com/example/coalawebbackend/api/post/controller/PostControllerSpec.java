package com.example.coalawebbackend.api.post.controller;

import com.example.coalawebbackend.api.post.dto.CreatePostResponse;
import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.api.post.dto.PostResponse;
import com.example.coalawebbackend.api.post.dto.UpdatePostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Post API", description = "게시글 관련 API")
public interface PostControllerSpec {

    @Operation(summary = "게시글 생성", description = "특정 게시판에 새로운 게시글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreatePostResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
    })
    ResponseEntity<CreatePostResponse> createPost(
            @Parameter(description = "게시판 ID", required = true)
            @PathVariable Long boardId,
            @Valid @RequestBody PostRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId
    );


    @Operation(summary = "게시글 목록 조회", description = "특정 게시판의 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
    })
    ResponseEntity<List<PostResponse>> getPosts(
            @Parameter(description = "게시판 ID", required = true)
            @PathVariable Long boardId
    );


    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 게시판을 찾을 수 없음")
    })
    ResponseEntity<PostResponse> getPostDetail(
            @Parameter(hidden = true)
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId
    );


    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UpdatePostResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<UpdatePostResponse> updatePost(
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId,
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request
    );


    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<Void> deletePost(
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId,
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId
    );
}
