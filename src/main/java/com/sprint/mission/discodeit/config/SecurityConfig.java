package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.security.CustomAuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;

    private final LoginFailureHandler loginFailureHandler;

    private final JwtLogoutHandler jwtLogoutHandler;

    private final CustomAuthenticationEntryPoint
            customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler
            customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
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

                        // 정적 리소스
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // CSRF 토큰
                        .requestMatchers(
                                "/api/auth/csrf-token"
                        ).permitAll()

                        // 로그인
                        .requestMatchers(
                                "/api/auth/login"
                        ).permitAll()

                        // Refresh Token을 이용한 Access Token 재발급
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/refresh"
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

                        // 그 외 요청은 인증 필요
                        .anyRequest()
                        .authenticated()
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
                 * JWT 기반 Stateless 인증
                 *
                 * 서버에서 HttpSession 기반 인증 상태를 유지하지 않고
                 * 매 요청마다 Access Token을 통해 인증한다.
                 *
                 * JWT의 활성 로그인 상태는 JwtRegistry에서 관리한다.
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Remember-Me는 제거되었다.
                 *
                 * REFRESH_TOKEN 쿠키와
                 * /api/auth/refresh API가
                 * 기존 Remember-Me 역할을 대신한다.
                 */
                .formLogin(login -> login
                        .loginProcessingUrl(
                                "/api/auth/login"
                        )
                        .successHandler(
                                jwtLoginSuccessHandler
                        )
                        .failureHandler(
                                loginFailureHandler
                        )
                )

                /*
                 * 로그아웃
                 *
                 * JwtLogoutHandler가
                 * JwtRegistry의 로그인 정보를 무효화하고
                 * REFRESH_TOKEN 쿠키도 삭제한다.
                 *
                 * 로그아웃 처리 완료 후에는
                 * 204 No Content를 반환한다.
                 */
                .logout(logout -> logout
                        .logoutUrl(
                                "/api/auth/logout"
                        )
                        .addLogoutHandler(
                                jwtLogoutHandler
                        )
                        .logoutSuccessHandler(
                                new HttpStatusReturningLogoutSuccessHandler(
                                        HttpStatus.NO_CONTENT
                                )
                        )
                )

                /*
                 * Authorization: Bearer {AccessToken}
                 *
                 * UsernamePasswordAuthenticationFilter보다 먼저 실행하여
                 * JWT 기반 인증 정보를 SecurityContext에 등록한다.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
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