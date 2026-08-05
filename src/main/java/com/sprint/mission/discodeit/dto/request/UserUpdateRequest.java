package com.sprint.mission.discodeit.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

    @Size(min = 1, max = 50, message = "사용자 이름은 1자 이상 50자 이하여야 합니다.")
    String newUsername,

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    String newEmail,

    @Size(min = 1, max = 60, message = "비밀번호는 1자 이상 60자 이하여야 합니다.")
    String newPassword

) {

}