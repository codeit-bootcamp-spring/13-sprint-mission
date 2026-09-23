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
import org.springframework.security.access.AccessDeniedException;

import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscodeitAccessDeniedHandler 테스트")
class DiscodeitAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("권한이 부족하면 403과 접근 거부 응답을 반환")
    void handle_accessDenied() throws Exception {
        // given
        DiscodeitAccessDeniedHandler handler =
                new DiscodeitAccessDeniedHandler(objectMapper);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AccessDeniedException exception =
                new AccessDeniedException(
                        "접근 권한이 없습니다."
                );

        // when
        handler.handle(
                request,
                response,
                exception
        );

        // then
        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN.value());

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

        ErrorResponse errorResponse =
                captor.getValue();

        assertThat(errorResponse.getCode())
                .isEqualTo("ACCESS_DENIED");

        assertThat(errorResponse.getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN.value());

        assertThat(errorResponse.getExceptionType())
                .isEqualTo("AccessDeniedException");

        assertThat(errorResponse.getDetails())
                .isEmpty();
    }
}