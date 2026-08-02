package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 사용자명입니다."),

    // Channel 관련
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채널입니다."),
    CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PRIVATE 채널은 수정할 수 없습니다."),

    // Message 관련
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메시지입니다."),

    // BinaryContent 관련
    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 첨부 파일입니다."),

    // Auth 관련
    AUTH_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),

    // UserStatus 관련
    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 상태입니다."),
    USER_STATUS_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 사용자 상태입니다."),

    // ReadStatus 관련
    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 읽음 상태입니다.");

    // HTTP 상태 코드
    private final HttpStatus status;

    // 클라이언트에게 보여줄 에러 메시지
    private final String message;
}