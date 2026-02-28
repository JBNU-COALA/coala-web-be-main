package com.example.coalawebbackend.api.board.controller;

import com.example.coalawebbackend.api.board.dto.BoardResponse;
import com.example.coalawebbackend.api.board.dto.CreateBoardRequest;
import com.example.coalawebbackend.api.board.dto.CreateBoardResponse;
import com.example.coalawebbackend.api.board.dto.UpdateBoardRequest;
import com.example.coalawebbackend.api.board.dto.UpdateBoardResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Board API", description = "게시판 관련 API")
public interface BoardControllerSpec {

    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시판 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateBoardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<CreateBoardResponse> createBoard(
            @Valid @RequestBody CreateBoardRequest request,
            String userId
    );

    @Operation(summary = "게시판 목록 조회", description = "게시판 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = BoardResponse.class))))
    })
    ResponseEntity<List<BoardResponse>> getBoards(
            @RequestParam(required = false) Boolean isActive
    );

    @Operation(summary = "게시판 수정", description = "게시판 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UpdateBoardResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
    })
    ResponseEntity<UpdateBoardResponse> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody UpdateBoardRequest request,
            String userId
    );

    @Operation(summary = "게시판 삭제", description = "게시판을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteBoard(
            @PathVariable Long boardId,
            String userId
    );
}
