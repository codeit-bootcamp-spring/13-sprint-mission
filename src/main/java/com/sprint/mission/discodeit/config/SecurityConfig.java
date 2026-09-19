package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      AuthenticationSuccessHandler loginSuccessHandler,
      AuthenticationEntryPoint restAuthenticationEntryPoint,
      AccessDeniedHandler restAccessDeniedHandler,
      SessionRegistry sessionRegistry,
      UserDetailsService userDetailsService
  ) throws Exception {

    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        )

        .formLogin(form -> form
            .loginProcessingUrl("/api/auth/login") // 폼이 POST 처리되는 URL(Spring이 가로챔)
            .successHandler(loginSuccessHandler)
            .permitAll() // 로그인 요청은 누구나 접근 가능
        )

        .rememberMe(rememberMe -> rememberMe
            .rememberMeParameter("remember-me")
            .userDetailsService(userDetailsService)
        )

        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")   // POST /logout 으로 로그아웃
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(
                HttpStatus.NO_CONTENT)) // 204
            .invalidateHttpSession(true)    // 세션 무효화(기본값이지만 명시)
            .deleteCookies("JSESSIONID", "remember-me") // 세션 쿠키 삭제, 자동 로그인 쿠키도 삭제
        )

        .authorizeHttpRequests(auth -> auth
            // ── 공개(permitAll) — 로그인 전에도 되어야 하는 것들 ──
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()  // CSRF 토큰 발급
            .requestMatchers(HttpMethod.POST, "/api/auth/").permitAll()           // 회원가입
            .requestMatchers("/api/auth/login", "/api/auth/logout")
            .permitAll()                        // 로그인·로그아웃 처리
            .requestMatchers("/", "/login.html", "/index.html", "/favicon.svg", "/assets/**")
            .permitAll() // 정적 리소스
            .requestMatchers("/h2-console/**", "my-api").permitAll()
            // Swagger UI 및 OpenAPI 문서
            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs",
                "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
            // Actuator
            .requestMatchers("/actuator", "/actuator/**").permitAll()
            // ── 그 외 전부 로그인 필요(기본 잠금) ──
            //   활동 쓰기(POST/PUT/DELETE)·현재 사용자(/me) 등은 자동으로 여기에 걸린다.
            //   소유권 등 세밀한 검사는 서비스의 @PreAuthorize 가 이어서 한다(두 겹 방어).
            .anyRequest().authenticated()
        )

        // 필터단에서 발생한 커스텀 예외 처리 등록 로직
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .accessDeniedHandler(restAccessDeniedHandler)
        )

        .sessionManagement(session -> session
            // 동시성 관련 설정은 이 블록 안에서 작성한다.
            .sessionConcurrency(concurrency -> concurrency
                .maximumSessions(1) // 한 사용자 당 최대 세션 수
                .maxSessionsPreventsLogin(
                    false) // false: 새 로그인 시 이전 세션 만료, true: 이미 로그인 되어 있다면 새 로그인 차단.
                .expiredUrl("/login.html?expired")
                .sessionRegistry(sessionRegistry)
            )
        )

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
        .role("ADMIN").implies("CHAANNEL_MANAGER", "USER")
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

  // 미인증(401) 응답을 ProblemDetail JSON으로 커스텀할 수 있는 객체.
  @Bean
  AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return (request, response, authException) ->
        writeProblem(objectMapper, response, HttpStatus.UNAUTHORIZED, "AUTH_401",
            "인증이 필요합니다. 로그인 후 다시 시도하세요.");
  }

  // 권한 부족(403) 응답을 ProblemDetail JSON으로 커스텀할 수 있는 객체.
  @Bean
  AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
    return (request, response, deniedException) ->
        writeProblem(objectMapper, response, HttpStatus.FORBIDDEN, "AUTH_403",
            "이 작업을 수행할 권한이 없습니다.");

  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  /**
   * 401/403 공통 — ProblemDetail 을 JSON 으로 직접 응답 본문에 쓴다.
   */
  private static void writeProblem(ObjectMapper objectMapper, HttpServletResponse response,
      HttpStatus status, String code, String detail) throws IOException {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setProperty("code", code);
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), pd);
  }

  // 서블릿 컨테이너의 세션 생성/소멸을 Spring 이벤트로 발행한다.
  // 동시 세션 제어에서도 세션 개수를 추적하려면 이 publisher가 필요하다.
  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

}
