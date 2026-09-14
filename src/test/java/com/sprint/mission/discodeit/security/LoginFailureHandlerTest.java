package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFailureHandler 테스트")
class LoginFailureHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인에 실패하면 401과 인증 실패 응답을 반환")
    void login_fail() throws Exception {
        // given
        LoginFailureHandler handler =
                new LoginFailureHandler(objectMapper);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BadCredentialsException exception =
                new BadCredentialsException("잘못된 인증 정보");

        // when
        handler.onAuthenticationFailure(
                request,
                response,
                exception
        );

        // then
        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());

        assertThat(response.getContentType())
                .startsWith(MediaType.APPLICATION_JSON_VALUE);

        assertThat(response.getCharacterEncoding())
                .isEqualTo("UTF-8");

        ArgumentCaptor<ErrorResponse> captor =
                ArgumentCaptor.forClass(ErrorResponse.class);

        then(objectMapper).should()
                .writeValue(
                        any(Writer.class),
                        captor.capture()
                );

        ErrorResponse errorResponse = captor.getValue();

        assertThat(errorResponse.getCode())
                .isEqualTo("AUTHENTICATION_FAILED");

        assertThat(errorResponse.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());

        assertThat(errorResponse.getExceptionType())
                .isEqualTo("BadCredentialsException");
    }
}