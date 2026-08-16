package com.sprint.mission.discodeit.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  USER_NOT_FOUND("존재하지 않는 사용자입니다."),
  DUPLICATE_USERNAME("이미 사용중인 username입니다."),
  DUPLICATE_EMAIL("이미 사용중인 email입니다."),

  CHANNEL_NOT_FOUND("존재하지 않는 채널입니다."),
  PRIVATE_CHANNEL_UPDATE("PRIVATE 채널은 수정할 수 없습니다."),

  MESSAGE_NOT_FOUND("존재하지 않는 메시지입니다."),

  READ_STATUS_NOT_FOUND("존재하지 않는 ReadStatus입니다."),
  READ_STATUS_ALREADY_EXISTS("이미 존재하는 ReadStatus입니다."),

  USER_STATUS_NOT_FOUND("존재하지 않는 UserStatus입니다."),
  USER_STATUS_ALREADY_EXISTS("이미 존재하는 UserStatus입니다."),

  BINARY_CONTENT_NOT_FOUND("존재하지 않는 파일입니다."),
  BINARY_CONTENT_READ_FAILED("파일을 읽는 중 오류가 발생했습니다."),

  INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}