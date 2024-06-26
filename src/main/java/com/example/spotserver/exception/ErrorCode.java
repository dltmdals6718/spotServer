package com.example.spotserver.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DUPLICATE_LOGINID(HttpStatus.CONFLICT,"중복된 아이디가 존재합니다."),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "중복된 닉네임이 존재합니다."),
    DUPLICATE_MAIL(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
    DUPLICATE_LIKE(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),
    FAIL_LOGIN(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호를 잘못 입력했습니다."),
    FAIL_MAIL_CERTIFICATION(HttpStatus.UNAUTHORIZED, "이메일 인증 번호가 틀렸습니다."),
    FAIL_MAIL_CERTIFICATION_REQUEST(HttpStatus.BAD_REQUEST, "이메일 인증 요청은 5분마다 가능합니다."),
    FAIL_MAIL_TIMEOUT(HttpStatus.BAD_REQUEST, "이메일 인증 가능 시간이 지났습니다."),
    FAIL_FILE_SIZE(HttpStatus.PAYLOAD_TOO_LARGE, "파일 용량 제한에 걸렸습니다."),
    NOT_MAIL_CERTIFICATION(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다."),
    NOT_VALID(HttpStatus.BAD_REQUEST, "올바른 입력을 해주세요."),
    NOT_VALID_MAIL(HttpStatus.BAD_REQUEST, "올바른 이메일 형식을 입력해주세요."),
    NOT_SUPPORT_FILE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자입니다."),
    UNAUTHORIZED_CLIENT(HttpStatus.UNAUTHORIZED, "접근 토큰이 없습니다."),
    FORBIDDEN_CLIENT(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    JWT_DECODE_FAIL(HttpStatus.UNAUTHORIZED, "올바른 토큰이 필요합니다."),
    JWT_SIGNATURE_FAIL(HttpStatus.UNAUTHORIZED, "올바른 토큰이 필요합니다."),
    JWT_LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다."),
    NOT_SUPPORTED_CONTENT_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "올바른 Content-Type으로 요청해주세요."),
    NO_SUCH_ELEMENT(HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다.");

    private HttpStatus httpStatus;
    private String message;


    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
