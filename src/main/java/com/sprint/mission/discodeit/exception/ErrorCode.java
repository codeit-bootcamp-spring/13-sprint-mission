package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USR-001",
            "사용자를 찾을 수 없습니다."
    ),
    USER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "USR-002",
            "이미 존재하는 사용자입니다."
    ),
    DUPLICATE_USERNAME(
            HttpStatus.CONFLICT,
            "USR-003",
            "이미 사용 중인 사용자 이름입니다."
    ),
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "USR-004",
            "이미 사용 중인 이메일입니다."
    ),
    INVALID_USER_REQUEST(
            HttpStatus.BAD_REQUEST,
            "USR-005",
            "사용자 요청이 올바르지 않습니다."
    ),

    // Channel
    CHANNEL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHN-001",
            "채널을 찾을 수 없습니다."
    ),
    PRIVATE_CHANNEL_UPDATE(
            HttpStatus.BAD_REQUEST,
            "CHN-002",
            "PRIVATE 채널은 수정할 수 없습니다."
    ),
    INVALID_CHANNEL_REQUEST(
            HttpStatus.BAD_REQUEST,
            "CHN-003",
            "채널 요청이 올바르지 않습니다."
    ),
    DUPLICATE_CHANNEL_PARTICIPANT(
            HttpStatus.CONFLICT,
            "CHN-004",
            "PRIVATE 채널 참여자가 중복되었습니다."
    ),
    CHANNEL_PARTICIPANT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHN-005",
            "PRIVATE 채널 참여자를 찾을 수 없습니다."
    ),

    // Message
    MESSAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MSG-001",
            "메시지를 찾을 수 없습니다."
    ),
    INVALID_MESSAGE(
            HttpStatus.BAD_REQUEST,
            "MSG-002",
            "메시지 요청이 올바르지 않습니다."
    ),
    INVALID_MESSAGE_CONTENT(
            HttpStatus.BAD_REQUEST,
            "MSG-003",
            "메시지 내용은 비어 있을 수 없습니다."
    ),
    MESSAGE_AUTHOR_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MSG-004",
            "메시지 작성자를 찾을 수 없습니다."
    ),
    MESSAGE_CHANNEL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MSG-005",
            "메시지를 작성할 채널을 찾을 수 없습니다."
    ),

    // BinaryContent
    BINARY_CONTENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "BIN-001",
            "바이너리 콘텐츠를 찾을 수 없습니다."
    ),
    INVALID_BINARY_CONTENT(
            HttpStatus.BAD_REQUEST,
            "BIN-002",
            "바이너리 콘텐츠 요청이 올바르지 않습니다."
    ),
    BINARY_CONTENT_STORAGE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "BIN-003",
            "파일 저장소 처리 중 오류가 발생했습니다."
    ),

    // Common
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "COM-001",
            "잘못된 요청입니다."
    ),
    METHOD_ARGUMENT_NOT_VALID(
            HttpStatus.BAD_REQUEST,
            "COM-002",
            "요청 값 검증에 실패했습니다."
    ),
    MESSAGE_NOT_READABLE(
            HttpStatus.BAD_REQUEST,
            "COM-003",
            "요청 본문을 읽을 수 없습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COM-004",
            "서버 내부 오류가 발생했습니다."
    ),
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COM-005",
            "요청한 리소스를 찾을 수 없습니다."
    );

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
