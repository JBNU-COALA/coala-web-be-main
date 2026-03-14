package com.example.coalawebbackend.common.enums;

import com.example.coalawebbackend.common.response.ApiResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@Schema(hidden = true)
public enum ErrorCode implements BaseCode {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 리프레시 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "잘못된 사용자 ID 형식입니다."),

    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다."),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 게시판에 대한 권한이 없습니다."),
    BOARD_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 게시판입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다." ),

    POST_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다." ),

    DUPLICATE_LIKE(HttpStatus.CONFLICT, "이미 좋아요를 누른 게시글입니다."),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
    @JsonIgnore
    @Schema(hidden = true)
    private final ApiResult apiResult;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.apiResult = ApiResult.builder()
                .success(false)
                .httpStatus(httpStatus)
                .message(message)
                .build();
    }

    @Override
    public ApiResult getReasonHttpStatus() {
        return apiResult;
    }
}
