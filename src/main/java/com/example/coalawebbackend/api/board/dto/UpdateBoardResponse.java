package com.example.coalawebbackend.api.board.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UpdateBoardResponse {

    private Long boardId;
    private String status;

    public static UpdateBoardResponse of(Long boardId) {
        return new UpdateBoardResponse(boardId, "UPDATED");
    }
}
