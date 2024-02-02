package com.example.spotserver.exception;

import lombok.Getter;

@Getter
public class PermissionException extends Exception{

    private ErrorCode errorCode;

    public PermissionException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
