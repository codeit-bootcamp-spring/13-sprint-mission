package com.sprint.mission.discodeit.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserCreateRequest(
        @Schema(description = "사용자 이름", examples = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @Schema(description = "이메일", examples = "hong@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "비밀번호", examples = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
)
{
    public UserCreateRequest{
        validate(username, "이름");
        validate(email, "이메일");
        validate(password, "비밀번호");
    }

    private static void validate(String value, String fieldName){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }
}


