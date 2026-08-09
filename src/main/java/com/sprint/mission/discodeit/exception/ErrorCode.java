package com.sprint.mission.discodeit.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    DUPLICATE_USER("이미 존재하는 사용자입니다."),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다."),

    CHANNEL_NOT_FOUND("존재하지 않는 채널입니다."),
    PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다."),

    MESSAGE_NOT_FOUND("존재하지 않는 메시지입니다."),

    READ_STATUS_NOT_FOUND("존재하지 않는 읽기 상태입니다."),
    READ_STATUS_ALREADY_EXISTS("해당 채널에 대한 읽기 상태가 이미 존재합니다."),

    USER_STATUS_NOT_FOUND("존재하지 않는 사용자 상태입니다."),
    USER_STATUS_ALREADY_EXISTS("해당 사용자의 상태 정보가 이미 존재합니다."),

    BINARY_CONTENT_NOT_FOUND("존재하지 않는 파일입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

}
