package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //입력값 검증
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    //유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    DUPLICATE_USER_NAME(HttpStatus.CONFLICT, "이미 존재하는 유저 이름입니다."),
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 유저 이메일입니다."),

    //채널 관련
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채널입니다."),
    PRIVATE_CHANNEL_UPDATE(HttpStatus.BAD_REQUEST, "Private 채널은 수정 불가능합니다."),

    //유저 상태 관련
    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저 상태입니다."),
    DUPLICATE_USER_STATUS(HttpStatus.CONFLICT, "이미 존재하는 유저 상태입니다."),

    //읽음 상태 관련
    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 읽음 상태입니다."),
    DUPLICATE_READ_STATUS(HttpStatus.CONFLICT, "이미 존재하는 읽음 상태입니다."),

    //BinaryContent 관련
    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 binaryContent입니다."),

    //메시지 관련
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메시지입니다."),

    //기타
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다."),
    FOLDER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "폴더 생성 중 오류가 발생했습니다."),
    STORED_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 파일을 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
