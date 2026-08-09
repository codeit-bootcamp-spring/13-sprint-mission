package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserUpdateRequest(

        @NotNull(message = "사용자 ID는 필수입니다.")
        UUID id,

        @NotBlank(message = "사용자 이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "사용자 이름은 2자 이상 20자 이하로 입력해주세요.")
        String userName,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @Valid
        BinaryContentCreateRequest profile
) {
}
