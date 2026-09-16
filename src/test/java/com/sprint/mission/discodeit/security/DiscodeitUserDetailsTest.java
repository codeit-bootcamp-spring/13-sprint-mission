package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DiscodeitUserDetails 테스트")
class DiscodeitUserDetailsTest {

    @Nested
    @DisplayName("사용자 정보 조회")
    class UserInfo {

        @Test
        @DisplayName("UserDto의 username을 반환")
        void getUsername_success() {
            // given
            UserDto userDto = createUserDto(
                    UUID.randomUUID(),
                    "user1",
                    Role.USER
            );

            DiscodeitUserDetails userDetails =
                    new DiscodeitUserDetails(
                            userDto,
                            "encoded-password"
                    );

            // when
            String username = userDetails.getUsername();

            // then
            assertThat(username)
                    .isEqualTo("user1");
        }

        @Test
        @DisplayName("저장된 비밀번호를 반환")
        void getPassword_success() {
            // given
            UserDto userDto = createUserDto(
                    UUID.randomUUID(),
                    "user1",
                    Role.USER
            );

            DiscodeitUserDetails userDetails =
                    new DiscodeitUserDetails(
                            userDto,
                            "encoded-password"
                    );

            // when
            String password = userDetails.getPassword();

            // then
            assertThat(password)
                    .isEqualTo("encoded-password");
        }

        @Test
        @DisplayName("UserDto를 반환")
        void getUserDto_success() {
            // given
            UserDto userDto = createUserDto(
                    UUID.randomUUID(),
                    "user1",
                    Role.USER
            );

            DiscodeitUserDetails userDetails =
                    new DiscodeitUserDetails(
                            userDto,
                            "encoded-password"
                    );

            // when & then
            assertThat(userDetails.getUserDto())
                    .isEqualTo(userDto);
        }
    }

    @Nested
    @DisplayName("권한 조회")
    class GetAuthorities {

        @Test
        @DisplayName("USER 역할을 ROLE_USER 권한으로 반환")
        void getAuthorities_user() {
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
        @DisplayName("CHANNEL_MANAGER 역할을 ROLE_CHANNEL_MANAGER 권한으로 반환")
        void getAuthorities_channelManager() {
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
        @DisplayName("ADMIN 역할을 ROLE_ADMIN 권한으로 반환")
        void getAuthorities_admin() {
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
    }

    @Nested
    @DisplayName("계정 상태")
    class AccountStatus {

        @Test
        @DisplayName("기본 계정 상태는 모두 활성 상태")
        void accountStatus_success() {
            // given
            DiscodeitUserDetails userDetails =
                    createUserDetails(Role.USER);

            // when & then
            assertThat(userDetails.isAccountNonExpired())
                    .isTrue();

            assertThat(userDetails.isAccountNonLocked())
                    .isTrue();

            assertThat(userDetails.isCredentialsNonExpired())
                    .isTrue();

            assertThat(userDetails.isEnabled())
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 비교")
    class Equality {

        @Test
        @DisplayName("동일한 객체는 동일하다고 판단")
        void equals_sameInstance() {
            // given
            DiscodeitUserDetails userDetails =
                    createUserDetails(Role.USER);

            // when & then
            assertThat(userDetails)
                    .isEqualTo(userDetails);
        }

        @Test
        @DisplayName("userId가 같으면 동일한 사용자로 판단")
        void equals_sameUserId() {
            // given
            UUID userId = UUID.randomUUID();

            DiscodeitUserDetails first =
                    new DiscodeitUserDetails(
                            createUserDto(
                                    userId,
                                    "user1",
                                    Role.USER
                            ),
                            "password1"
                    );

            DiscodeitUserDetails second =
                    new DiscodeitUserDetails(
                            createUserDto(
                                    userId,
                                    "changed-name",
                                    Role.ADMIN
                            ),
                            "password2"
                    );

            // when & then
            assertThat(first)
                    .isEqualTo(second);
        }

        @Test
        @DisplayName("userId가 다르면 다른 사용자로 판단")
        void equals_differentUserId() {
            // given
            DiscodeitUserDetails first =
                    new DiscodeitUserDetails(
                            createUserDto(
                                    UUID.randomUUID(),
                                    "user1",
                                    Role.USER
                            ),
                            "password"
                    );

            DiscodeitUserDetails second =
                    new DiscodeitUserDetails(
                            createUserDto(
                                    UUID.randomUUID(),
                                    "user1",
                                    Role.USER
                            ),
                            "password"
                    );

            // when & then
            assertThat(first)
                    .isNotEqualTo(second);
        }

        @Test
        @DisplayName("DiscodeitUserDetails가 아닌 객체와는 동일하지 않음")
        void equals_otherType() {
            // given
            DiscodeitUserDetails userDetails =
                    createUserDetails(Role.USER);

            // when & then
            assertThat(userDetails)
                    .isNotEqualTo("user1");
        }

        @Test
        @DisplayName("userId가 같으면 hashCode도 동일")
        void hashCode_sameUserId() {
            // given
            UUID userId = UUID.randomUUID();

            DiscodeitUserDetails first =
                    new DiscodeitUserDetails(
                            createUserDto(
                                    userId,
                                    "user1",
                                    Role.USER
                            ),
                            "password1"
                    );

            DiscodeitUserDetails second =
                    new DiscodeitUserDetails(
                            createUserDto(
                                    userId,
                                    "user2",
                                    Role.ADMIN
                            ),
                            "password2"
                    );

            // when & then
            assertThat(first.hashCode())
                    .isEqualTo(second.hashCode());
        }
    }

    private DiscodeitUserDetails createUserDetails(Role role) {
        UserDto userDto = createUserDto(
                UUID.randomUUID(),
                "user1",
                role
        );

        return new DiscodeitUserDetails(
                userDto,
                "encoded-password"
        );
    }

    private UserDto createUserDto(
            UUID userId,
            String username,
            Role role
    ) {
        return new UserDto(
                userId,
                username,
                username + "@test.com",
                null,
                true,
                role
        );
    }
}