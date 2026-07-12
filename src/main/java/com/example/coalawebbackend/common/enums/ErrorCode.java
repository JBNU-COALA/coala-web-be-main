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
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다."),
    INVALID_EMAIL_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않거나 만료되었습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "잘못된 사용자 ID 형식입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    DUPLICATE_STUDENT_ID(HttpStatus.CONFLICT, "이미 등록된 학번입니다."),
    DUPLICATE_GITHUB_ID(HttpStatus.CONFLICT, "이미 등록된 GitHub 아이디입니다."),
    DUPLICATE_LINKEDIN_URL(HttpStatus.CONFLICT, "이미 등록된 LinkedIn 프로필입니다."),

    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다."),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 게시판에 대한 권한이 없습니다."),
    BOARD_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 게시판입니다."),
    BOARD_NOT_ANONYMOUS(HttpStatus.BAD_REQUEST, "익명 게시판이 아닙니다."),
    DOMAIN_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신청된 도메인 주소입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_NOT_EDITABLE(HttpStatus.CONFLICT, "수정할 수 없는 게시글 상태입니다."),
    POST_NOT_DELETABLE(HttpStatus.CONFLICT, "삭제할 수 없는 게시글 상태입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNSAFE_CONTENT(HttpStatus.BAD_REQUEST, "허용되지 않는 HTML 또는 스크립트가 포함되어 있습니다."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    USER_SANCTIONED(HttpStatus.FORBIDDEN, "현재 활동 제한 상태입니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다." ),
    COMMENT_NOT_EDITABLE(HttpStatus.CONFLICT, "수정할 수 없는 댓글 상태입니다."),

    POST_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다." ),

    DUPLICATE_LIKE(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."),
    INVALID_ATTACHMENT(HttpStatus.BAD_REQUEST, "첨부파일 요청이 올바르지 않습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다."),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "이미 신고한 대상입니다.");

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
