package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 50, message = "비밀번호는 50자 이하여야 합니다.")
    String newPassword,
    @Size(max = 30, message = "사용자 이름은 30자 이하여야 합니다.")
    String newUsername,
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
    String newEmail,
    @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.") String phoneNumber,
    Role role
) {

}
