package com.sprint.mission.discodeit.config;


import com.sprint.mission.discodeit.security.CsrfTokenHandler;
import com.sprint.mission.discodeit.security.userdetail.LoginFailureHandler;
import com.sprint.mission.discodeit.security.userdetail.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity  // 정확하게 어떤 부분을 건드리나?
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler
    ) throws Exception {
        http
                .csrf( csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenHandler())
                )

                // 로그인 실제 페이지.
                .formLogin( login -> login
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                );

        return http.build();    // security 설정 적용(build)
    }

    /*
    password beans
     */
    @Bean
    public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }



}
