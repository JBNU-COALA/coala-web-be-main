package com.example.coalawebbackend.api.auth.exception;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;

public class AuthException extends CustomException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
