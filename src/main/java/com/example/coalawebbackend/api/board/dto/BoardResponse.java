package com.example.coalawebbackend.api.board.dto;

import com.example.coalawebbackend.domain.board.entity.Board;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardResponse {

    private Long boardId;
    private String boardName;
    private String boardType;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .boardId(board.getBoardId())
                .boardName(board.getName())
                .boardType(board.getType())
                .description(board.getDescription())
                .isActive(board.getIsActive())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
