package com.example.coalawebbackend.common.exception;

import com.example.coalawebbackend.common.response.ApiResponse;
import com.example.coalawebbackend.common.response.ApiResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<ApiResult>> handleApiException(CustomException e) {
        return ApiResponse.onFailure(e.getErrorCode());
    }
}
