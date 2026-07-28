package com.sprint.mission.discodeit.exception;

public enum ErrorCode {
    USER_NOT_FOUND("존재하지 않는 유저입니다."),
    DUPLICATE_USER("이미 사용 중인 유저입니다."),
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다."),

    CHANNEL_NOT_FOUND("존재하지 않는 채널입니다."),
    PRIVATE_CHANNEL_UPDATE("PRIVATE 채널은 수정할 수 없습니다."),

    MESSAGE_NOT_FOUND("존재하지 않는 메시지입니다."),
    INVALID_MESSAGE_CONTENT("메시지 내용 또는 첨부파일이 필요합니다."),

    BINARY_CONTENT_NOT_FOUND("존재하지 않는 파일입니다."),
    BINARY_CONTENT_STORAGE_ERROR("파일 저장소 처리 중 오류가 발생했습니다."),

    READ_STATUS_NOT_FOUND("존재하지 않는 읽음 상태입니다."),
    USER_STATUS_NOT_FOUND("존재하지 않는 유저 상태입니다."),
    DUPLICATE_USER_STATUS("이미 유저 상태가 존재합니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
