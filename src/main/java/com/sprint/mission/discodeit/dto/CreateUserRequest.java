package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(
            min = 2,
            max = 50,
            message = "사용자 이름은 2자 이상 50자 이하여야 합니다."
    )
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(
            max = 100,
            message = "이메일은 100자 이하여야 합니다."
    )
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(
            min = 8,
            max = 100,
            message = "비밀번호는 8자 이상 100자 이하여야 합니다."
    )
    private String password;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}