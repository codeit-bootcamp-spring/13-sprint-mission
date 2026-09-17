package com.sprint.mission.discodeit.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.security.CsrfTokenHandler;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;

@Configuration
@EnableWebSecurity  // 정확하게 어떤 부분을 건드리나?
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            SessionRegistry sessionRegistry

    ) throws Exception {
        http
                .csrf( csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenHandler())
                )

                .authorizeHttpRequests( auth -> auth
                        // 특정 경로 인증 안함.
                        .requestMatchers(HttpMethod.GET,"/api/auth/csrf-token").permitAll() // csrf 토큰 발급
                        .requestMatchers(HttpMethod.POST,"/api/users").permitAll() // 유저 생성 (회원가입)
                        .requestMatchers("/api/auth/login","/api/auth/logout").permitAll() //csrf 토큰 발급
                        .requestMatchers("/", "/login.html", "/index.html", "/favicon.svg", "/assets/**").permitAll() // 정적 리소스
                        .requestMatchers("/h2-console/**", "/swagger-doc").permitAll() // h2 인메모리 데이터베이스, swagger
                        // 권한 기반 페이지 인가
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMIN")
                        // 기본 인증 필요
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/logout.html?expired")
                        // 세션 갱신
                        .sessionFixation(fix -> fix.changeSessionId())
                        // 동시 세션 관리
                        .sessionConcurrency(concur -> concur
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .expiredUrl("/logout.html?expired")
                                .sessionRegistry(sessionRegistry)
                        )
                )

                // 에러 핸들러
                .exceptionHandling( e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 로그인 설정
                .formLogin( login -> login
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )

                // 로그아웃 설정
                .logout( logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                );

        return http.build();    // security 설정 적용(build)
    }

    /*
    password beans
     */
    @Bean
    public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }


    @Bean
    public SessionRegistry sessionRegistry(){ return new SessionRegistryImpl(); }

    @Bean
    public RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("CHANNEL_MANAGER")
                .role("CHANNEL_MANAGER").implies("USER")
                .build();
    }


    @Bean
    AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper){
        return (req,res,auth) ->
                writeProblem(objectMapper, res, HttpStatus.FORBIDDEN,"A_403","권한이 없습니다.");
    }

    @Bean
    AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, deniedException) ->
                writeProblem(objectMapper, response, HttpStatus.FORBIDDEN, "A_403", "권한이 없습니다.");

    }

    private static void writeProblem(ObjectMapper objectMapper, HttpServletResponse response,
                                     HttpStatus status, String code, String detail) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setProperty("code", code);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), pd);
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() { return new HttpSessionEventPublisher(); }


}
