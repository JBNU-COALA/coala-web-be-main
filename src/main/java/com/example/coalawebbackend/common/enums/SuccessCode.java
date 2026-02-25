package com.example.coalawebbackend.common.enums;

import com.example.coalawebbackend.common.response.ApiResult;
import org.springframework.http.HttpStatus;

public enum SuccessCode  implements BaseCode {

    ;

    private final ApiResult apiResult;

    SuccessCode(HttpStatus httpStatus, String message) {
        this.apiResult = ApiResult.builder()
                .success(true)
                .httpStatus(httpStatus)
                .message(message)
                .build();

    }

    @Override
    public ApiResult getReasonHttpStatus() {
        return apiResult;
    }

}