package com.sprint.mission.discodeit.security.userdetail;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
로그인에 성공했을 때, 자동적으로 응답을 반환하는 Controller 레이어 객체. Spring ? or Tomcat ? 의 반환값으로 사용.
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    // 각 답변에 대한 구조, 확인 필요.

    // AI 답변
    // 로그인 처리 후 요청을 거부/응답으로 끝내지 않고,
    // "뒤이어 위치한 서블릿 필터나 Controller(DispatcherServlet)로 요청을 계속 진행(Forward/Chain)시켜야 할 때" 사용.
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }


    // AI 답변
    // 단순 로그인 페이지 리다이렉트나 JSON 응답 전송과 같은 일반적인 폼 로그인/REST API 로그인 환경에서 사용됨.
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        // authentication 내부 구조?
        DiscodeitUserDetails principal = (DiscodeitUserDetails) authentication.getPrincipal();

        // 응답을 response 에 직접 설정하는 방식으로 응답.
        // tomcat 방식이라?
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8"); // ascii 이외 문자 인코딩 호환성.
        objectMapper.writeValue(response.getWriter(), principal.getUserDto()); // userDto 응답으로 반환
    }
}
