package com.example.coalawebbackend.api.resource.controller;

import com.example.coalawebbackend.api.resource.dto.CreateResourceRequest;
import com.example.coalawebbackend.api.resource.dto.ResourceResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Resource API", description = "자료 관련 API")
public interface ResourceControllerSpec {

    @Operation(summary = "자료 업로드", description = "게시글에 자료를 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = ResourceResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<ResourceResponse> createResource(
            @PathVariable Long postId,
            @Valid @RequestBody CreateResourceRequest request,
            @Parameter(hidden = true) String userId
    );

    @Operation(summary = "자료 목록 조회", description = "게시글의 자료 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ResourceResponse.class)))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<List<ResourceResponse>> getResources(
            @PathVariable Long postId
    );

    @Operation(summary = "자료 삭제", description = "자료를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "자료를 찾을 수 없음")
    })
    ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId,
            @Parameter(hidden = true) String userId
    );
}
