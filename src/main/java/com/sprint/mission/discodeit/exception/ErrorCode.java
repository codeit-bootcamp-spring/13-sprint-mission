package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS("이미 존재하는 사용자입니다."),
    DUPLICATE_USERNAME("이미 사용 중인 사용자 이름입니다."),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다."),
    INVALID_USER_REQUEST("사용자 요청이 올바르지 않습니다."),

    // Channel
    CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다."),
    PRIVATE_CHANNEL_UPDATE("PRIVATE 채널은 수정할 수 없습니다."),
    INVALID_CHANNEL_REQUEST("채널 요청이 올바르지 않습니다."),
    DUPLICATE_CHANNEL_PARTICIPANT("PRIVATE 채널 참여자가 중복되었습니다."),
    CHANNEL_PARTICIPANT_NOT_FOUND("PRIVATE 채널 참여자를 찾을 수 없습니다."),

    // Message
    MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다."),
    INVALID_MESSAGE("메시지 요청이 올바르지 않습니다."),
    INVALID_MESSAGE_CONTENT("메시지 내용은 비어 있을 수 없습니다."),
    MESSAGE_AUTHOR_NOT_FOUND("메시지 작성자를 찾을 수 없습니다."),
    MESSAGE_CHANNEL_NOT_FOUND("메시지를 작성할 채널을 찾을 수 없습니다."),

    // BinaryContent
    BINARY_CONTENT_NOT_FOUND("바이너리 콘텐츠를 찾을 수 없습니다."),
    INVALID_BINARY_CONTENT("바이너리 콘텐츠 요청이 올바르지 않습니다."),
    BINARY_CONTENT_STORAGE_ERROR("파일 저장소 처리 중 오류가 발생했습니다."),

    // Common
    INVALID_REQUEST("잘못된 요청입니다."),
    METHOD_ARGUMENT_NOT_VALID("요청 값 검증에 실패했습니다."),
    MESSAGE_NOT_READABLE("요청 본문을 읽을 수 없습니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;
}