package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채널 입니다."),
    DUPLICATE_CHANNEL(HttpStatus.CONFLICT, "이미 존재하는 채널명입니다."),
    PRIVATE_CHANNEL_UPDATE(HttpStatus.BAD_REQUEST, "비공개 채널은 수정 불가합니다."),

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 메시지 입니다."),
    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 파일입니다."),

    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 상태입니다."),
    USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 사용자의 상태 정보가 이미 존재합니다."),

    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 읽음 상태입니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

    STORAGE_PUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다."),
    STORAGE_GET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 불러오지 못했습니다."),
    STORAGE_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "스토리지 초기화에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
