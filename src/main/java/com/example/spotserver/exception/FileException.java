package com.example.spotserver.exception;

import lombok.Getter;

@Getter
public class FileException extends Exception{

    private ErrorCode errorCode;

    public FileException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
