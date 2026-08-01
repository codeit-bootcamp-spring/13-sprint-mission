package com.sprint.mission.discodeit.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Schema(description = "로그인 정보")
public class LoginRequest { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언

  @NotBlank(message = "사용자 이름은 필수입니다.")
  @Size(min = 2, max = 50, message = "사용자 이름은 2자 이상 50자 이하만 가능합니다.")
  private String username;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 8, max = 60, message = "비밀번호는 8자 이상 60자 이하만 가능합니다.")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).*$", message = "비밀번호는 최소한 하나의 소문자(a-z), 하나의 대문자(A-Z), 하나의 숫자(0-9), 하나의 특수 문자(!@#$%^&*)를 포함해야 합니다.")
  private String password;
}
