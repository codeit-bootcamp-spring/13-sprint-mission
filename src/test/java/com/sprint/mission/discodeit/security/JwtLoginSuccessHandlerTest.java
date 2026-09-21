package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.handler.JwtLoginSuccessHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtLoginSuccessHandler 단위 테스트")
class JwtLoginSuccessHandlerTest {

    private static final String ACCESS_TOKEN = "issued-access-token";
    private static final String REFRESH_TOKEN = "issued-refresh-token";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private static final String RAW_PASSWORD = "raw-password";
    private static final long REFRESH_TOKEN_LIFETIME_SECONDS = Duration.ofDays(14).getSeconds();

    @Mock
    JwtTokenProvider jwtTokenProvider;

    ObjectMapper objectMapper;
    JwtLoginSuccessHandler jwtLoginSuccessHandler;
    UserDto userDto;
    Authentication authentication;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        jwtLoginSuccessHandler = new JwtLoginSuccessHandler(
                jwtTokenProvider,
                objectMapper,
                new RefreshCookieProperties(true, false, "Lax")
        );
        OffsetDateTime now = OffsetDateTime.parse("2026-09-10T10:00:00+09:00");
        userDto = new UserDto(
                UUID.randomUUID(),
                "테스트사용자",
                "test@example.com",
                null,
                true,
                Role.CHANNEL_MANAGER,
                now,
                now
        );
        DiscodeitUserDetails principal = new DiscodeitUserDetails(userDto, ENCODED_PASSWORD);
        authentication = new UsernamePasswordAuthenticationToken(
                principal, RAW_PASSWORD, principal.getAuthorities()
        );
        given(jwtTokenProvider.generateAccessToken(userDto.id(), userDto.username(), userDto.role()))
                .willReturn(ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(userDto.id(), userDto.username()))
                .willReturn(REFRESH_TOKEN);
        given(jwtTokenProvider.getRefreshTokenLifetime()).willReturn(REFRESH_TOKEN_LIFETIME_SECONDS);
    }

    @Test
    @DisplayName("인증된 사용자의 ID, 사용자명, 역할로 액세스 토큰과 리프레시 토큰을 발급한다")
    void onAuthenticationSuccess_issuesTokensForAuthenticatedUser() throws Exception {
        authenticate();

        verify(jwtTokenProvider).generateAccessToken(userDto.id(), userDto.username(), userDto.role());
        verify(jwtTokenProvider).generateRefreshToken(userDto.id(), userDto.username());
    }

    @Test
    @DisplayName("인증에 성공하면 200 응답과 JwtDto를 UTF-8 JSON으로 반환한다")
    void onAuthenticationSuccess_returnsJwtDto() throws Exception {
        MockHttpServletResponse response = authenticate();

        JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        JwtDto responseBody = objectMapper.treeToValue(body, JwtDto.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(body.size()).isEqualTo(2);
        assertThat(body.has("userDto")).isTrue();
        assertThat(responseBody.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(responseBody.userDto())
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(userDto);
        assertThat(responseBody.userDto().createdAt().toInstant()).isEqualTo(userDto.createdAt().toInstant());
        assertThat(responseBody.userDto().updatedAt().toInstant()).isEqualTo(userDto.updatedAt().toInstant());
    }

    @ParameterizedTest(name = "Secure={0}, SameSite={1}")
    @CsvSource({
            "false, Lax",
            "true, Lax",
            "true, Strict",
            "true, None"
    })
    @DisplayName("리프레시 쿠키에 주입된 Secure와 SameSite 설정 및 만료 시간을 적용한다")
    void onAuthenticationSuccess_setsRefreshTokenCookieUsingConfiguredAttributes(
            boolean secure, String sameSite
    ) throws Exception {
        jwtLoginSuccessHandler = new JwtLoginSuccessHandler(
                jwtTokenProvider,
                objectMapper,
                new RefreshCookieProperties(true, secure, sameSite)
        );
        MockHttpServletResponse response = authenticate();

        Cookie refreshTokenCookie = response.getCookie("REFRESH_TOKEN");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isEqualTo(REFRESH_TOKEN);
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
        assertThat(refreshTokenCookie.getSecure()).isEqualTo(secure);
        assertThat(refreshTokenCookie.getAttribute("SameSite")).isEqualTo(sameSite);
        assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(Math.toIntExact(REFRESH_TOKEN_LIFETIME_SECONDS));
    }

    @Test
    @DisplayName("응답 Body에 리프레시 토큰과 비밀번호를 포함하지 않는다")
    void onAuthenticationSuccess_doesNotExposeRefreshTokenOrPasswordInBody() throws Exception {
        MockHttpServletResponse response = authenticate();

        String responseBody = response.getContentAsString(StandardCharsets.UTF_8);
        JsonNode body = objectMapper.readTree(responseBody);
        assertThat(body.findValues("refreshToken")).isEmpty();
        assertThat(body.findValues("password")).isEmpty();
        assertThat(responseBody).doesNotContain(REFRESH_TOKEN, ENCODED_PASSWORD, RAW_PASSWORD);
    }

    private MockHttpServletResponse authenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtLoginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        return response;
    }
}
