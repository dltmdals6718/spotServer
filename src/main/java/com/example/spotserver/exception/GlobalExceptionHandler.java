package com.example.spotserver.exception;

import com.example.spotserver.domain.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> argumentValid(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(ErrorCode.NOT_VALID.name());
        if(e.hasErrors()) {
            errorResponse.setMessage(e.getAllErrors().get(0).getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ErrorResponse> mediaTypeException(HttpMediaTypeNotSupportedException e) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_SUPPORTED_CONTENT_TYPE);
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(errorResponse);
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> noSuchElementException(NoSuchElementException e) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NO_SUCH_ELEMENT);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler({PermissionException.class})
    public ResponseEntity<ErrorResponse> permissionException(PermissionException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorResponse> authenticationException(AuthenticationException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(value = DuplicateException.class)
    public ResponseEntity<ErrorResponse> duplicateException(DuplicateException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(value = MailException.class)
    public ResponseEntity<ErrorResponse> mailException(MailException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(value = FileException.class)
    public ResponseEntity<ErrorResponse> fileException(FileException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(value = MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> multiPartException(MissingServletRequestPartException e) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_VALID);
        errorResponse.setMessage(e.getRequestPartName() + "를 입력해주세요.");
        return ResponseEntity
                .status(e.getStatusCode())
                .body(errorResponse);
    }

}
