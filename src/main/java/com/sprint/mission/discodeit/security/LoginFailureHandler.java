package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.exception.user.WrongPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {

    ErrorResponse errorResponse;

    if (exception instanceof BadCredentialsException
        || exception instanceof UsernameNotFoundException) {

      WrongPasswordException wrongPasswordException = new WrongPasswordException();

      errorResponse = ErrorResponse.from(wrongPasswordException, HttpStatus.UNAUTHORIZED.value());

    } else {
      ErrorCode errorCode = ErrorCode.AUTHENTICATION_FAILED;

      errorResponse = new ErrorResponse(
          Instant.now(),
          errorCode.getCode(),
          errorCode.getMessage(),
          Map.of(),
          errorCode.getStatus().value(),
          exception.getClass().getSimpleName()
      );
    }

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getWriter(), errorResponse);

  }
}
