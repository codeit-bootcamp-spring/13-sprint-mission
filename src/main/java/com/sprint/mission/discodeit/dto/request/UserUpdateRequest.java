package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UserUpdateRequest(

    @Size(min = 3, max = 20)
    String newUsername,
    @Email
    String newEmail,
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{12,24}$", message = "비밀번호는 12~24자이며, 숫자 1개 이상, 영문 1개 이상을 포함해야합니다.")
    String newPassword,
    UUID newProfileId) {

}
