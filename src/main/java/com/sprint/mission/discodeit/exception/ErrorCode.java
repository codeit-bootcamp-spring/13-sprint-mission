package com.sprint.mission.discodeit.exception;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode  {

    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    DUPLICATE_USER("이미 존재하는 사용장입니다."),

    CHANNEL_NOT_FOUND("채널을 칮을 수 없습니다."),
    PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다."),

    MESSAGE_NOT_FOUND("메세지를 찾을 수 없습니다."),

    BINARY_CONTENT_NOT_FOUND("파일을 찾을 수 없습니다."),

    INVALID_REQUEST("잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

}
