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
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscodeitAuthenticationEntryPoint 테스트")
class DiscodeitAuthenticationEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("인증되지 않은 사용자가 접근하면 401과 인증 필요 응답을 반환")
    void commence_unauthenticated() throws Exception {
        // given
        DiscodeitAuthenticationEntryPoint entryPoint =
                new DiscodeitAuthenticationEntryPoint(objectMapper);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        InsufficientAuthenticationException exception =
                new InsufficientAuthenticationException(
                        "인증이 필요합니다."
                );

        // when
        entryPoint.commence(
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
                .isEqualTo("AUTHENTICATION_REQUIRED");

        assertThat(errorResponse.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());

        assertThat(errorResponse.getExceptionType())
                .isEqualTo("InsufficientAuthenticationException");

        assertThat(errorResponse.getDetails())
                .isEmpty();
    }
}