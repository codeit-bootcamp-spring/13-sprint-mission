package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("토큰 갱신 실패는 401 상태와 전용 오류 코드로 응답한다")
    void handleDiscodeitException_returnsUnauthorizedForTokenRenewalFailure() {
        TokenRenewalFailedException exception = new TokenRenewalFailedException();

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleDiscodeitException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().code()).isEqualTo("TOKEN_RENEWAL_FAILED");
        assertThat(response.getBody().message()).isEqualTo("토큰 갱신에 실패했습니다.");
    }

    @Test
    @DisplayName("validation details keep all messages for the same field")
    void handleValidation_groupsMessagesByField() throws Exception {
        // given
        Method method = ValidationTarget.class.getDeclaredMethod("validate", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new ValidationTarget(), "validationTarget");
        bindingResult.addError(new FieldError("validationTarget", "username", "first message"));
        bindingResult.addError(new FieldError("validationTarget", "username", "second message"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        // when
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleValidation(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().details())
                .containsEntry("username", List.of("first message", "second message"));
    }

    private static class ValidationTarget {
        @SuppressWarnings("unused")
        void validate(String username) {
        }
    }
}
