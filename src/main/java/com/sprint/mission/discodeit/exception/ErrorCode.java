package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // User
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다."
    ),
    USER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "USER_ALREADY_EXISTS",
            "이미 존재하는 사용자입니다."
    ),

    // Channel
    CHANNEL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHANNEL_NOT_FOUND",
            "채널을 찾을 수 없습니다."
    ),
    CHANNEL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "CHANNEL_ALREADY_EXISTS",
            "이미 존재하는 채널입니다."
    ),
    PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST,
            "PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED",
            "PRIVATE 채널은 수정할 수 없습니다."
    ),

    // Message
    MESSAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MESSAGE_NOT_FOUND",
            "메시지를 찾을 수 없습니다."
    ),

    // BinaryContent
    BINARY_CONTENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "BINARY_CONTENT_NOT_FOUND",
            "파일을 찾을 수 없습니다."
    ),
    FILE_UPLOAD_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FILE_UPLOAD_FAILED",
            "파일 업로드에 실패했습니다."
    ),

    // Validation
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "요청 값이 올바르지 않습니다."
    ),

    // Common
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}