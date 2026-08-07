package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
  WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "아이디 또는 비밀번호가 올바르지 않습니다."),
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "U004", "사용자 상태를 찾을 수 없습니다."),

  // Channel
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE(HttpStatus.FORBIDDEN, "CH002", "비공개 채널은 수정할 수 없습니다."),

  // Message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "메시지를 찾을 수 없습니다."),

  // ReadStatus
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "RS001", "읽음 상태를 찾을 수 없습니다."),
  READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "RS002", "이미 읽음 상태가 존재합니다."),

  // BinaryContent
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "바이너리 콘텐츠를 찾을 수 없습니다."),
  BINARY_CONTENT_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "B002", "파일 업로드 중 오류가 발생했습니다."),
  BINARY_CONTENT_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "B003", "파일 다운로드 중 오류가 발생했습니다."),
  BINARY_CONTENT_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "B004", "파일 삭제에 실패했습니다."),

  // Common
  ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "요청한 API를 찾을 수 없습니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
