package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.service.TokenService;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 갱신 단위 테스트")
class AuthControllerUnitTest {

    @Mock
    UserRoleUpdater userRoleUpdater;

    @Mock
    TokenService tokenService;

    AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(userRoleUpdater, tokenService);
    }

    @Test
    @DisplayName("갱신된 JwtDto만 본문으로 반환하고 새 리프레시 토큰은 쿠키 설정에 전달한다")
    void refreshToken_returnsJwtDtoAndDelegatesCookieSetting() {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-21T00:00:00Z");
        UserDto user = new UserDto(UUID.randomUUID(), "refresh-user", "refresh@example.com",
                null, true, Role.USER, now, now);
        JwtDto jwtDto = new JwtDto(user, "new-access");
        given(tokenService.rotateRefreshToken())
                .willReturn(new JwtDtoWithRefresh(jwtDto, "new-refresh"));

        ResponseEntity<JwtDto> response = controller.refreshToken();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(jwtDto);
        verify(tokenService).rotateRefreshToken();
        verify(tokenService).addRefreshTokenCookie("new-refresh");
    }

    @Test
    @DisplayName("갱신 실패 예외를 전파하고 새 쿠키를 설정하지 않는다")
    void refreshToken_doesNotSetCookieWhenRenewalFails() {
        TokenRenewalFailedException failure = new TokenRenewalFailedException();
        given(tokenService.rotateRefreshToken()).willThrow(failure);

        assertThatThrownBy(controller::refreshToken).isSameAs(failure);

        verify(tokenService, never()).addRefreshTokenCookie(any());
    }
}
