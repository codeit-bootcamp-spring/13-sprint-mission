package com.sprint.mission.discodeit.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "수정할 User 정보")
public class UserUpdateRequest { // 수정 대상 객체의 id 파라미터, 수정할 값 파라미터

  @Size(min = 2, max = 50, message = "수정할 사용자 이름은 2자 이상 50자 이하만 가능합니다.")
  @NotBlank(message = "수정할 사용자 이름은 필수입니다.")
  private String newUsername;

  @NotBlank(message = "수정할 이메일은 필수입니다.")
  @Email(message = "수정할 이메일 형식을 준수해야 합니다.")
  private String newEmail;

  @NotBlank(message = "수정할 비밀번호는 필수입니다.")
  @Size(min = 8, max = 60, message = "수정할 비밀번호는 8자 이상 60자 이하만 가능합니다.")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).*$", message = "수정할 비밀번호는 최소한 하나의 소문자(a-z), 하나의 대문자(A-Z), 하나의 숫자(0-9), 하나의 특수 문자(!@#$%^&*)를 포함해야 합니다.")
  private String newPassword;
}
