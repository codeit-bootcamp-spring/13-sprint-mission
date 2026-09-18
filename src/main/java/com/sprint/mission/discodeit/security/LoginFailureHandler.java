package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.exception.user.WrongPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {

    WrongPasswordException wrongPasswordException = new WrongPasswordException();

    ErrorResponse errorResponse = ErrorResponse.from(
        wrongPasswordException, HttpStatus.UNAUTHORIZED.value()
    );

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    objectMapper.writeValue(
        response.getWriter(),
        errorResponse
    );

  }
}
