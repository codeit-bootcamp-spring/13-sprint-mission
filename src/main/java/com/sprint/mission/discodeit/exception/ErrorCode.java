package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  DUPLICATE_USER("이미 존재하는 사용자입니다."),
  WRONG_PASSWORD("아이디 또는 비밀번호가 올바르지 않습니다."),
  USER_STATUS_NOT_FOUND("사용자 상태를 찾을 수 없습니다."),

  CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다."),

  MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다."),

  READ_STATUS_NOT_FOUND("읽음 상태를 찾을 수 없습니다."),
  READ_STATUS_ALREADY_EXISTS("이미 읽음 상태가 존재합니다."),

  BINARY_CONTENT_NOT_FOUND("바이너리 콘텐츠를 찾을 수 없습니다."),
  FILE_READ_FAILED("파일을 읽는 중 오류가 발생했습니다."),

  INVALID_REQUEST("잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

  private final String message;

}
