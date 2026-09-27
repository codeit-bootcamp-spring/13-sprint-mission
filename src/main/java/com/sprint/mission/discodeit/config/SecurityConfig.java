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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;

@Configuration
@EnableWebSecurity  // 정확하게 어떤 부분을 건드리나?
// @PreAuthor 를 위함.
// AOP 를 통한 매핑이라, Mvc 테스트에서 프록시 객체가 컨트롤러를 향하는 요청을 가로챈다.
// 다만, JDK 프록시는 인터페이스 매핑이라 ControllerDoc 으로 요청을 보내서 404 에러(요청 매핑이 없음 -> 구현체에 존재) 가 생긴다.
//@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler,
//            PersistentTokenRepository tokenRepository,
            UserDetailsService userDetailsService,
            AuthenticationSuccessHandler loginSuccessHandler,
            AuthenticationFailureHandler loginFailureHandler
//            SessionRegistry sessionRegistry

    ) throws Exception {
        http
                .csrf( csrf -> csrf
                        .disable()
                )

                .authorizeHttpRequests( auth -> auth
                        // 특정 경로 인증 안함.
                        .requestMatchers(HttpMethod.GET,"/api/auth/csrf-token").permitAll() // csrf 토큰 발급
                        .requestMatchers(HttpMethod.POST,"/api/users").permitAll() // 유저 생성 (회원가입)
                        .requestMatchers("/api/auth/login","/api/auth/logout").permitAll()
                        .requestMatchers("/", "/login.html", "/index.html", "/favicon.ico", "/assets/**").permitAll() // 정적 리소스
                        .requestMatchers("/h2-console/**", "/swagger-doc").permitAll() // h2 인메모리 데이터베이스, swagger
                        // 권한 기반 페이지 인가
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMIN")
                        // 기본 인증 필요
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 에러 핸들러
                .exceptionHandling( e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 로그인 설정 - 클라이언트가 로그인을 폼 로그인을 사용해서 시도.
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
    AuthenticationSuccessHandler loginSuccessHandler(ObjectMapper objectMapper) {
        return new LoginSuccessHandler(objectMapper);
    }

    @Bean
    AuthenticationFailureHandler loginFailureHandler(ObjectMapper objectMapper) {
        return new LoginFailureHandler(objectMapper);
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
