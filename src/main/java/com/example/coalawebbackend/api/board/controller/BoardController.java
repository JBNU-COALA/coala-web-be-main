package com.example.coalawebbackend.api.board.controller;

import com.example.coalawebbackend.api.board.dto.BoardResponse;
import com.example.coalawebbackend.api.board.dto.CreateBoardRequest;
import com.example.coalawebbackend.api.board.dto.CreateBoardResponse;
import com.example.coalawebbackend.api.board.dto.UpdateBoardRequest;
import com.example.coalawebbackend.api.board.dto.UpdateBoardResponse;
import com.example.coalawebbackend.api.board.facade.BoardFacade;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController implements BoardControllerSpec{

    private final BoardFacade boardFacade;

    @PostMapping
    public ResponseEntity<CreateBoardResponse> createBoard(
            @Valid @RequestBody CreateBoardRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardFacade.createBoard(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoards(
            @RequestParam(required = false) Boolean isActive
    ) {
        List<BoardResponse> response =
                boardFacade.getBoards(isActive);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(
            @PathVariable Long boardId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BoardResponse.from(boardFacade.getBoard(boardId)));
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<UpdateBoardResponse> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody UpdateBoardRequest request,
            @AuthenticationPrincipal String userId
    ) {
        UpdateBoardResponse response =
                boardFacade.updateBoard(boardId, request, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal String userId
    ) {
        boardFacade.deleteBoard(boardId, userId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
