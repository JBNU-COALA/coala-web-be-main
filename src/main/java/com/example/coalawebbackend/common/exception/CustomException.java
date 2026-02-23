package com.example.coalawebbackend.common.exception;

import com.example.coalawebbackend.common.enums.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

    private final BaseCode errorCode;

}
