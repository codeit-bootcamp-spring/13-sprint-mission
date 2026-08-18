package com.sprint.mission.discodeit.entity;

public record UserData(
        String username,
        String email,
        String password
) {

    public UserData {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "사용자 이름은 비어 있을 수 없습니다."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "이메일은 비어 있을 수 없습니다."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "비밀번호는 비어 있을 수 없습니다."
            );
        }
    }
}