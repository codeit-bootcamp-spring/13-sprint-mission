package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  // Spring Security의 요청 처리 규칙을 적용하는 빈
  // Spring boot 기본 자동 설정 대신 사용자 정의 설정을 적용할 수 있다
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // 클라이언트에서 쿠키에 저장된 CSRF 토큰을 접근할 수 있도록 httpOnly=false 로 설정
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        );
    return http.build();
  }
}
