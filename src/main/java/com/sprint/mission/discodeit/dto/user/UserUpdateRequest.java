package com.sprint.mission.discodeit.dto.user;

public record UserUpdateRequest(
    String name,
    String email,
    String password
)
{
    public UserUpdateRequest {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (email != null && email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        if (password != null && password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
    }
}
