package com.example.coalawebbackend.api.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateBoardRequest {

    @NotBlank(message = "게시판 이름은 필수입니다.")
    @Size(max = 50, message = "게시판 이름은 50자 이내여야 합니다.")
    private String boardName;

    @NotBlank(message = "게시판 타입은 필수입니다.")
    @Pattern(regexp = "NORMAL|RECRUIT|ANONYMOUS", message = "게시판 타입은 NORMAL, RECRUIT, ANONYMOUS만 가능합니다.")
    private String boardType;

    @Size(max = 255, message = "설명은 255자 이내여야 합니다.")
    private String description;


}
