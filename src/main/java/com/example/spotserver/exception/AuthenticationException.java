package com.example.spotserver.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends Exception {
    private ErrorCode errorCode;

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
