package com.example.coalawebbackend.api.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
