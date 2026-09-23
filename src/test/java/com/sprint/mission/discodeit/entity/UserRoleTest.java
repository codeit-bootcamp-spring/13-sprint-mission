package com.sprint.mission.discodeit.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void 새로운_사용자의_기본_권한은_USER이다() {
        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void 사용자_권한을_변경한다() {
        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        user.updateRole(UserRole.CHANNEL_MANAGER);

        assertThat(user.getRole())
                .isEqualTo(UserRole.CHANNEL_MANAGER);
    }
}