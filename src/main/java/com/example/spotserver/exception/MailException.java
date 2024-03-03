package com.example.spotserver.exception;

import lombok.Getter;

@Getter
public class MailException extends Exception {

    private ErrorCode errorCode;

    public MailException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }


}
