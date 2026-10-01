package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

public enum ExceptionCode {
    FILE_SYSTEM_ERROR("F001",INTERNAL_SERVER_ERROR,"File System Error"),

    // 바이너리 컨텐츠 에러
    BINARY_CONTENT_NOT_FOUND("B001", NOT_FOUND, "해당 리소스를 조회 하는데 오류가 발생 했습니다.."),

    // 유저 에러
    USER_NOT_FOUND("U001", NOT_FOUND,"유저를 조회하는데 오류가 발생 했습니다."),

    // 메세지 에러
    MESSAGE_NOT_FOUND("M001",NOT_FOUND,"메세지를 조회하는데 오류가 발생 했습니다."),

    // ReadStatus 에러
    READ_STATUS_NOT_FOUND_ERROR("RS001",NOT_FOUND,"속성 조회중 오류가 발생 했습니다."),
    READ_STATUS_DUPLICATE_ERROR("RS002",BAD_REQUEST,"속성 생성중 오류가 발생 했습니다."),

    // 인증, 인가 에러.
    INCORRECT_CERT_ERROR("A001",UNAUTHORIZED,"로그인 정보가 올바르지 않습니다."),
    INVALID_AUTH_ERROR("C002",FORBIDDEN,"권한이 없습니다."),

    // 엔드포인트 에러
    REQUEST_VALUE_ERROR("C003",BAD_REQUEST,"요청 정보가 올바르지 않습니다."),
    RESOURCE_NOT_FOUNDED("C001",NOT_FOUND,"요청 리소스가 존재하지 않습니다."),

    // 기본 서버 에러
    SERVER_ERROR("C999",INTERNAL_SERVER_ERROR,"예기치 못한 에러.");


    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ExceptionCode(String code, HttpStatus status, String defaultMessage){
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String code(){
        return code;
    }

    public HttpStatus status(){
        return status;
    }

    public String defaultMessage(){
        return defaultMessage;
    }
}
