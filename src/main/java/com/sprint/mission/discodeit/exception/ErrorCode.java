package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_EMAIL_DUPLICATED("USER_EMAIL_DUPLICATED", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_USERNAME_DUPLICATED("USER_USERNAME_DUPLICATED", HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다."),
    USER_LOGIN_FAILED("USER_LOGIN_FAILED", HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),

    TOKEN_RENEWAL_FAILED("TOKEN_RENEWAL_FAILED", HttpStatus.UNAUTHORIZED, "토큰 갱신에 실패했습니다."),

    USER_STATUS_NOT_FOUND("USER_STATUS_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자 상태를 찾을 수 없습니다."),
    USER_STATUS_ALREADY_EXISTS("USER_STATUS_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 사용자 상태가 존재합니다."),

    CHANNEL_NOT_FOUND("CHANNEL_NOT_FOUND", HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
    CHANNEL_TYPE_REQUIRED("CHANNEL_TYPE_REQUIRED", HttpStatus.BAD_REQUEST, "채널 타입은 필수입니다."),
    CHANNEL_TYPE_INVALID("CHANNEL_TYPE_INVALID", HttpStatus.BAD_REQUEST, "지원하지 않는 채널 타입입니다."),
    PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED("PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED", HttpStatus.BAD_REQUEST, "PRIVATE 채널은 수정할 수 없습니다."),

    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
    MESSAGE_INVALID_PAGING_CONDITION("MESSAGE_INVALID_PAGING_CONDITION", HttpStatus.BAD_REQUEST, "메시지 페이징 조건이 올바르지 않습니다."),
    MESSAGE_FILE_SAVE_FAILED("MESSAGE_FILE_SAVE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "메시지 첨부 파일 저장에 실패했습니다."),

    READ_STATUS_NOT_FOUND("READ_STATUS_NOT_FOUND", HttpStatus.NOT_FOUND, "읽음 상태를 찾을 수 없습니다."),
    READ_STATUS_ALREADY_EXISTS("READ_STATUS_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 읽음 상태가 존재합니다."),

    FILE_SAVE_FAILED("FILE_SAVE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다."),
    FILE_READ_FAILED("FILE_READ_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽는 중 오류가 발생했습니다."),
    FILE_DIRECTORY_CREATE_FAILED("FILE_DIRECTORY_CREATE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 디렉터리 생성 중 오류가 발생했습니다."),
    FILE_DELETE_FAILED("FILE_DELETE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다."),
    FILE_NOT_FOUND("FILE_NOT_FOUND", HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

    INVALID_REQUEST("INVALID_REQUEST", HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
