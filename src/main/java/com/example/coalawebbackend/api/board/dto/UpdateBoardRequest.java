package com.example.coalawebbackend.api.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateBoardRequest {

    @NotBlank(message = "게시판 이름은 필수입니다.")
    @Size(max = 50, message = "게시판 이름은 50자 이내여야 합니다.")
    private String boardName;

    @Size(max = 255, message = "설명은 255자 이내여야 합니다.")
    private String description;

    private Boolean isActive;
}
