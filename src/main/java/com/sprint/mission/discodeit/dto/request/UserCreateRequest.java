package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UserCreateRequest(

    @Size(min = 3, max = 20)
    @NotBlank(message = "userName을 입력해주세요.")
    String username,

    @NotBlank(message = "Email을 입력해주세요.")
    @Email
    String email,

    @NotBlank(message = "password를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{12,24}$",
        message = "비밀번호는 12~24자이며, 숫자 1개 이상, 영문 1개 이상을 포함해야합니다.")
    String password,
    UUID profileId) {

}
