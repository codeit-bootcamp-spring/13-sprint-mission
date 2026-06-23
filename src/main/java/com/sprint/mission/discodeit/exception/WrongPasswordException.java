package com.sprint.mission.discodeit.exception;

public class WrongPasswordException extends RuntimeException {

  public WrongPasswordException() {
    super("아이디 또는 비밀번호가 잘못 되었습니다.");
  }
}
