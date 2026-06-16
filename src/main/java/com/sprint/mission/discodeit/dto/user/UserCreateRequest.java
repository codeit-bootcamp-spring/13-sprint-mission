package com.sprint.mission.discodeit.dto.user;

public record UserCreateRequest(
        String name,
        String email,
        String password
)
{
    public UserCreateRequest{
        validate(name, "이름");
        validate(email, "이메일");
        validate(password, "비밀번호");
    }

    private static void validate(String value, String fieldName){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }
}


