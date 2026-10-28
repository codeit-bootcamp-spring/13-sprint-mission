package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity // Method Security 활성화
@EnableConfigurationProperties(AdminProperties.class)
public class SecurityConfig {

  // Spring Security의 요청 처리 규칙을 적용하는 빈
  // Spring boot 기본 자동 설정 대신 사용자 정의 설정을 적용할 수 있다
  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler,
      LoginFailureHandler loginFailureHandler,
      ObjectMapper objectMapper
  ) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // 클라이언트에서 쿠키에 저장된 CSRF 토큰을 접근할 수 있도록 httpOnly=false 로 설정
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        .authorizeHttpRequests(auth -> auth
            // 인증 없이 접근할 수 있는 API
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
            .requestMatchers(HttpMethod.POST,
                "/api/users",
                "/api/auth/login",
                "/api/auth/logout"
            ).permitAll()
            // API가 아닌 요청(정적 리소스, Swagger, Actuator 등)
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers(
                "/",
                "/index.html",
                "/favicon.ico",
                "/assets/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/api-docs",
                "/api-docs/**",
                "/actuator",
                "/actuator/**",
                "/error"
            ).permitAll()
            .anyRequest().authenticated() // 모든 요청에 대해 인증 필요
        )
        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/login")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)
        )
        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT) // 204 반환
            )
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, exception) ->
                writeSecurityError(response, objectMapper, ErrorCode.AUTHENTICATION_REQUIRED,
                    exception.getClass().getSimpleName())) // 인증되지 않은 사용자 처리
            .accessDeniedHandler((request, response, exception) ->
                writeSecurityError(response, objectMapper, ErrorCode.ACCESS_DENIED,
                    exception.getClass().getSimpleName())) // 인증은 되었지만 권한이 없는 사용자 처리
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // 비밀번호를 단방향 해시로 인코딩 / 검증하는 데 사용되는 PasswordEncoder 빈을 생성
  }

  private static void writeSecurityError(HttpServletResponse response, ObjectMapper objectMapper,
      ErrorCode errorCode, String exceptionType) throws IOException {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .details(Map.of())
        .exceptionType(exceptionType)
        .status(errorCode.getStatus().value())
        .build();

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
