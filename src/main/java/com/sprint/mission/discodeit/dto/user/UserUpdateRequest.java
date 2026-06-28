package com.sprint.mission.discodeit.dto.user;

public record UserUpdateRequest(
    String newUsername,
    String newEmail,
    String newPassword
)
{
    public UserUpdateRequest {
        if (newUsername != null && newUsername.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (newEmail != null && newEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        if (newPassword != null && newPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
    }
}
