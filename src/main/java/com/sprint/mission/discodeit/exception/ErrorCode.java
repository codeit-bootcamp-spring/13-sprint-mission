package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode { // 예외 코드명, 메시지 정의
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음"),
  DUPLICATE_USER(HttpStatus.BAD_REQUEST, "이메일 또는 사용자 이름이 같은 사용자가 이미 존재함"),

  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없음"),
  PRIVATE_CHANNEL_UPDATE(HttpStatus.BAD_REQUEST, "비공개 채널은 수정할 수 없음"),

  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없음"),
  AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없음"),

  CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "첨부 파일을 찾을 수 없음"),

  CONTENT_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "첨부 파일 업로드에 실패함"),

  READSTATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 읽음 상태를 찾을 수 없음"),
  DUPLICATE_READSTATUS(HttpStatus.BAD_REQUEST, "읽음 상태가 이미 존재함"),

  USERSTATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 상태를 찾을 수 없음"),
  DUPLICATE_USERSTATUS(HttpStatus.BAD_REQUEST, "사용자 상태가 이미 존재함"),

  WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않음"),

  NO_CHANGES(HttpStatus.BAD_REQUEST, "변경사항 없음"),

  PARAM_ERROR(HttpStatus.BAD_REQUEST, "입력 검증 실패"),
  MESSAGE_CONVERTER_ERROR(HttpStatus.BAD_REQUEST, "요청 본문(JSON)을 읽을 수 없음"),
  UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 문제 발생");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
