package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.security.CustomAuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    private final CustomAuthenticationEntryPoint
            customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler
            customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            SessionRegistry sessionRegistry,
            DiscodeitUserDetailsService discodeitUserDetailsService
    ) throws Exception {

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository
                                        .withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new SpaCsrfTokenRequestHandler()
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CSRF 토큰 발급
                        .requestMatchers(
                                "/api/auth/csrf-token"
                        ).permitAll()

                        // 로그인
                        .requestMatchers(
                                "/api/auth/login"
                        ).permitAll()

                        // 로그아웃
                        .requestMatchers(
                                "/api/auth/logout"
                        ).permitAll()

                        // 회원가입
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Actuator
                        .requestMatchers(
                                "/actuator/**"
                        ).permitAll()

                        // 그 외 모든 요청 인증 필요
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                customAuthenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                customAccessDeniedHandler
                        )
                )

                /*
                 * 동일 계정의 활성 세션은 최대 1개만 유지한다.
                 *
                 * maximumSessions(1)
                 * -> 사용자당 활성 세션 최대 1개
                 *
                 * maxSessionsPreventsLogin(false)
                 * -> 새로운 로그인을 막지 않고
                 *    기존 세션을 만료시킨다.
                 *
                 * Remember-Me로 새로운 인증 세션이 생성되는 경우에도
                 * 기존 세션과 충돌하지 않도록 한다.
                 */
                .sessionManagement(management -> management
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .sessionRegistry(sessionRegistry)
                        )
                )

                /*
                 * 로그인 유지(Remember-Me)
                 *
                 * 로그인 요청:
                 * remember-me=true
                 *
                 * JSESSIONID가 없어져도 remember-me 쿠키가 존재하면
                 * 사용자를 다시 인증한다.
                 */
                .rememberMe(remember -> remember
                        .key(
                                "discodeit-remember-me-key"
                        )
                        .rememberMeParameter(
                                "remember-me"
                        )
                        .rememberMeCookieName(
                                "remember-me"
                        )
                        .tokenValiditySeconds(
                                60 * 60 * 24 * 14
                        )
                        .userDetailsService(
                                discodeitUserDetailsService
                        )
                )

                .formLogin(login -> login
                        .loginProcessingUrl(
                                "/api/auth/login"
                        )
                        .successHandler(
                                loginSuccessHandler
                        )
                        .failureHandler(
                                loginFailureHandler
                        )
                )

                .logout(logout -> logout
                        .logoutUrl(
                                "/api/auth/logout"
                        )
                        .logoutSuccessHandler(
                                new HttpStatusReturningLogoutSuccessHandler(
                                        HttpStatus.NO_CONTENT
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {

        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher
    httpSessionEventPublisher() {

        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {

        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_CHANNEL_MANAGER
                ROLE_CHANNEL_MANAGER > ROLE_USER
                """);
    }

    @Bean
    static MethodSecurityExpressionHandler
    methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy
    ) {

        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();

        handler.setRoleHierarchy(
                roleHierarchy
        );

        return handler;
    }
}