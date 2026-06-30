package com.sprint.mission.discodeit.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Schema(description = "로그인 정보")
public class LoginRequest { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언

  private String username;
  private String password;
}
