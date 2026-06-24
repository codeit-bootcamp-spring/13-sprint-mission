package com.sprint.mission.discodeit.dto.auth;

public record LoginRequest(
        String username,
        String password
)
{
    public LoginRequest {
        validate(username, "이름");
        validate(password, "비밀번호");
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }

}
