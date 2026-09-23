package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginFailureHandler 단위 테스트")
class LoginFailureHandlerTest {

    ObjectMapper objectMapper;
    LoginFailureHandler loginFailureHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        loginFailureHandler = new LoginFailureHandler(objectMapper);
    }

    @Test
    @DisplayName("인증에 실패하면 표준 401 오류 응답을 JSON으로 반환한다")
    void onAuthenticationFailure_returnsUnauthorizedError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        loginFailureHandler.onAuthenticationFailure(request, response, exception);

        JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(body.path("status").asInt()).isEqualTo(401);
        assertThat(body.path("exceptionType").asText()).isEqualTo("UserLoginFailedException");
        assertThat(body.path("code").asText()).isEqualTo("USER_LOGIN_FAILED");
        assertThat(body.path("message").asText()).isEqualTo("아이디 또는 비밀번호가 일치하지 않습니다.");
        assertThat(body.path("details").isEmpty()).isTrue();
        assertThat(body.path("timestamp").asText()).isNotBlank();
    }
}
