package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.error.ErrorResponse;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.SpaCsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtLoginSuccessHandler jwtLoginSuccessHandler,
      JwtLogoutHandler jwtLogoutHandler,
      AuthenticationFailureHandler loginFailureHandler,
      AuthenticationEntryPoint restAuthenticationEntryPoint,
      AccessDeniedHandler restAccessDeniedHandler
  ) throws Exception {

    http
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/notifications/**", "/ws", "/ws/**")
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )

        .formLogin(form -> form
            .loginProcessingUrl("/api/auth/login") // 폼이 POST 처리되는 URL(Spring이 가로챔)
            .successHandler(jwtLoginSuccessHandler)
            .failureHandler(loginFailureHandler)
            .permitAll() // 로그인 요청은 누구나 접근 가능
        )

        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")   // POST /logout 으로 로그아웃
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(
                HttpStatus.NO_CONTENT)) // 204
            .addLogoutHandler(jwtLogoutHandler)
            .invalidateHttpSession(true)    // 세션 무효화(기본값이지만 명시)
            .deleteCookies("JSESSIONID")
        )

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/ws", "/ws/**").permitAll()
            // ── 공개(permitAll) — 로그인 전에도 되어야 하는 것들 ──
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()  // CSRF 토큰 발급
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()          // 회원가입
            .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/refresh")
            .permitAll()                        // 로그인·로그아웃 처리
            .requestMatchers("/", "/login.html", "/index.html", "/favicon.svg", "/assets/**")
            .permitAll() // 정적 리소스
            .requestMatchers("/h2-console/**", "/my-api").permitAll()
            // Swagger UI 및 OpenAPI 문서
            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs",
                "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
            // Actuator
            .requestMatchers("/actuator", "/actuator/**").permitAll()
            // ── 그 외 전부 로그인 필요(기본 잠금) ──
            //   활동 쓰기(POST/PUT/DELETE) 등은 자동으로 여기에 걸린다.
            //   소유권 등 세밀한 검사는 서비스의 @PreAuthorize 가 이어서 한다(두 겹 방어).
            .anyRequest().authenticated()
        )

        // 필터단에서 발생한 커스텀 예외 처리 등록 로직
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .accessDeniedHandler(restAccessDeniedHandler)
        )

        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

    ;

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("ADMIN").implies("CHANNEL_MANAGER", "USER")
        .role("CHANNEL_MANAGER").implies("USER")
        .build();
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  // 미인증(401) 응답을 ErrorResponse JSON으로 반환한다.
  @Bean
  AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return (request, response, authException) ->
        writeError(objectMapper, response, HttpStatus.UNAUTHORIZED, "AUTH_401",
            "인증이 필요합니다. 로그인 후 다시 시도하세요.", authException);
  }

  // 권한 부족(403) 응답을 ErrorResponse JSON으로 반환한다.
  @Bean
  AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
    return (request, response, deniedException) ->
        writeError(objectMapper, response, HttpStatus.FORBIDDEN, "AUTH_403",
            "이 작업을 수행할 권한이 없습니다.", deniedException);

  }

  /**
   * 필터에서 발생한 401/403도 ErrorResponse 형식으로 반환한다.
   */
  private static void writeError(ObjectMapper objectMapper, HttpServletResponse response,
      HttpStatus status, String code, String detail, Exception exception) throws IOException {
    ErrorResponse error = new ErrorResponse(Instant.now(), code, detail, Map.of(),
        exception.getClass().getSimpleName(), status.value());
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), error);
  }

}
