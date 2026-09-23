package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DiscodeitUserDetails 테스트")
class DiscodeitUserDetailsTest {

    @Test
    @DisplayName("USER 권한은 ROLE_USER 권한으로 변환된다")
    void should_ReturnRoleUser_when_UserRoleIsUser() {

        // given
        DiscodeitUserDetails userDetails =
                createUserDetails(Role.USER);

        // when
        Collection<? extends GrantedAuthority> authorities =
                userDetails.getAuthorities();

        // then
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("CHANNEL_MANAGER 권한은 ROLE_CHANNEL_MANAGER 권한으로 변환된다")
    void should_ReturnRoleChannelManager_when_UserRoleIsChannelManager() {

        // given
        DiscodeitUserDetails userDetails =
                createUserDetails(Role.CHANNEL_MANAGER);

        // when
        Collection<? extends GrantedAuthority> authorities =
                userDetails.getAuthorities();

        // then
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CHANNEL_MANAGER");
    }

    @Test
    @DisplayName("ADMIN 권한은 ROLE_ADMIN 권한으로 변환된다")
    void should_ReturnRoleAdmin_when_UserRoleIsAdmin() {

        // given
        DiscodeitUserDetails userDetails =
                createUserDetails(Role.ADMIN);

        // when
        Collection<? extends GrantedAuthority> authorities =
                userDetails.getAuthorities();

        // then
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    private DiscodeitUserDetails createUserDetails(Role role) {

        UserResponse userResponse =
                new UserResponse(
                        UUID.randomUUID(),
                        Instant.now(),
                        null,
                        "testUser",
                        "test@test.com",
                        role,
                        null,
                        false
                );

        return new DiscodeitUserDetails(
                userResponse,
                "$2a$10$encodedPassword"
        );
    }
}