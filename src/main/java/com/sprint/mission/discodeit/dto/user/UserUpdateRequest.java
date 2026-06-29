package com.sprint.mission.discodeit.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateRequest(
    @Schema(description = "수정할 사용자 이름", example = "홍길이",  requiredMode = Schema.RequiredMode.REQUIRED)
    String newUsername,
    @Schema(description = "수정할 이메일", example = "aaa@example.com",  requiredMode = Schema.RequiredMode.REQUIRED)
    String newEmail,
    @Schema(description = "수정할 비밀번호", example = "0000",  requiredMode = Schema.RequiredMode.REQUIRED)
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
