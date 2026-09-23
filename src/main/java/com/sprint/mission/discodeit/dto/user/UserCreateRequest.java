package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 30, message = "비밀번호는 30자 이하여야 합니다.")
    String password,
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(max = 20, message = "사용자 이름은 20자 이하여야 합니다.")
    String username,
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
    String email,
    @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
    String phoneNumber,
    Role role
) {

  public User toEntity() {
    return User.builder()
        .password(password)
        .username(username)
        .email(email)
        .phoneNumber(phoneNumber)
        .role(Role.USER) // 회원가입 시 모든 사용자는 USER 권한을 기본으로
        .build();
  }
}
