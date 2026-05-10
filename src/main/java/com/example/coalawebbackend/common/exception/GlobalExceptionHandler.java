package com.example.coalawebbackend.common.exception;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.response.ApiResponse;
import com.example.coalawebbackend.common.response.ApiResult;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<ApiResult>> handleApiException(CustomException e) {
        return ApiResponse.onFailure(e.getErrorCode());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<ApiResult>> handleValidationException(Exception e) {
        return ApiResponse.onFailure(ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<ApiResult>> handleNotFoundException(NoSuchElementException e) {
        return ApiResponse.onFailure(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
