package com.example.coalawebbackend.api.board.dto;

import com.example.coalawebbackend.domain.board.entity.Board;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateBoardResponse {

    private Long boardId;
    private String boardName;
    private LocalDateTime createdAt;

    public static CreateBoardResponse from(Board board) {
        return new CreateBoardResponse(
                board.getBoardId(),
                board.getName(),
                board.getCreatedAt()
        );
    }
}
