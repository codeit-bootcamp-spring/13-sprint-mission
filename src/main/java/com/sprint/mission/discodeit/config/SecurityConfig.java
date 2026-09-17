package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.ApiErrorResponse;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.security.handler.LoginSuccessHandler;
import com.sprint.mission.discodeit.security.handler.SpaCsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            AuthenticationEntryPoint restAuthenticationEntryPoint,
            AccessDeniedHandler restAccessDeniedHandler,
            SessionRegistry sessionRegistry,
            UserDetailsService userDetailsService,
            RememberMeProperties rememberMeProperties
    ) throws Exception {

        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/csrf-token",
                                "/api/auth/login",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/default-avatar.png",
                                "/assets/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api/docs/**"
                        ).permitAll()
                        .requestMatchers("/actuator", "/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/role").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .sessionManagement(session -> session
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .sessionRegistry(sessionRegistry)
                        )
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("remember-me")
                        .key(rememberMeProperties.key())
                        .tokenValiditySeconds(rememberMeProperties.tokenValiditySeconds())
                        .userDetailsService(userDetailsService)
                )
                .build();
    }

    // 미인증(401) 응답을 ApiErrorResponse JSON으로 커스텀할 수 있는 객체
    @Bean
    AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) ->
                writeApiErrorResponse(
                        objectMapper,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "AUTH_401",
                        "인증이 필요합니다. 로그인 후 다시 시도해 주세요."
                );
    }

    // 권한 부족(403) 응답을 ApiErrorResponse JSON으로 커스텀할 수 있는 객체
    @Bean
    AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, deniedException) ->
                writeApiErrorResponse(
                        objectMapper,
                        response,
                        HttpStatus.FORBIDDEN,
                        "AUTH_403",
                        "이 작업을 수행할 권한이 없습니다."
                );
    }

    /** 401/403 공통 — ApiErrorResponse를 JSON으로 직접 응답 본문에 쓴다. */
    private static void writeApiErrorResponse(ObjectMapper objectMapper, HttpServletResponse response,
                                              HttpStatus status, String code, String detail) throws IOException {
        ApiErrorResponse apiErrorResponse = ApiErrorResponse.of(
                status.value(),
                status.name(),
                code,
                detail,
                Map.of()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), apiErrorResponse);
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name())
                .implies(Role.CHANNEL_MANAGER.name())
                .role(Role.CHANNEL_MANAGER.name())
                .implies(Role.USER.name())
                .build();
    }

    /*
     * Spring Security 6.3 이전에는 RoleHierarchyImpl Builder API가 없어
     * 객체를 직접 생성하고 setHierarchy로 계층을 등록했다.
     * 현재 버전에서는 해당 생성자와 setHierarchy가 deprecated 상태이다.
     *
     * @Bean
     * static RoleHierarchy roleHierarchy() {
     *     RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
     *     roleHierarchy.setHierarchy(
     *             "ROLE_ADMIN > ROLE_CHANNEL_MANAGER\n"
     *                     + "ROLE_CHANNEL_MANAGER > ROLE_USER"
     *     );
     *     return roleHierarchy;
     * }
     *
     * RoleHierarchy를 메서드 보안 표현식 핸들러에 명시적으로 연결하는 예시이다.
     * 현재 Spring Security 6.5에서는 RoleHierarchy Bean을 자동으로 감지하므로 Bean으로 등록하지 않는다.
     *
     * @Bean
     * static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
     *         RoleHierarchy roleHierarchy
     * ) {
     *     DefaultMethodSecurityExpressionHandler handler =
     *             new DefaultMethodSecurityExpressionHandler();
     *     handler.setRoleHierarchy(roleHierarchy);
     *     return handler;
     * }
     */

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
