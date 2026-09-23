package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiscodeitUserDetailsTest {

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void 사용자_권한을_GrantedAuthority로_변환한다(
            UserRole role
    ) {
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "codeit",
                "codeit@example.com",
                false,
                null,
                role
        );

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(
                        userResponse,
                        "encoded-password"
                );

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + role.name());
    }
}