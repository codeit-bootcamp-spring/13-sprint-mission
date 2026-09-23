package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 데이터 유효성 검증에 실패했습니다."),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
  USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 유저 정보입니다."),
  INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "유저 데이터가 올바르지 않습니다."),
  USER_ARCHIVE_FAILED(HttpStatus.CONFLICT, "유저 데이터를 저장할 수 없습니다."),
  PROFILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "프로필 이미지 크기 제한을 초과합니다."),
  UNSUPPORT_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 프로필 이미지 형식입니다."),


  INVALID_CHANNEL_INPUT(HttpStatus.BAD_REQUEST, "채널 입력값이 올바르지 않습니다."),
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  CHANNEL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 채널 이름입니다."),
  CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "수정할 수 없는 채널입니다."),
  CHANNEL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채널에 접근할 권한이 없습니다."),
  CHANNEL_ARCHIVE_FAILED(HttpStatus.CONFLICT, "채널 데이터를 저장할 수 없습니다."),

  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
  INVALID_MESSAGE_INPUT(HttpStatus.BAD_REQUEST, "메시지 입력값이 올바르지 않습니다."),
  MESSAGE_ARCHIVE_FAILED(HttpStatus.CONFLICT, "메시지 데이터를 저장할 수 없습니다."),

  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽음 상태를 찾을 수 없습니다."),

  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "파일 정보를 찾을 수 없습니다."),
  BINARY_CONTENT_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 문제가 발생했습니다."),

  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
