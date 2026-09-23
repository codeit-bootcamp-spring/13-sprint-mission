package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import com.sprint.mission.discodeit.security.RestAccessDeniedHandler;
import com.sprint.mission.discodeit.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${discodeit.remember-me.key}")
  private String rememberMeKey;

  @Value("${discodeit.remember-me.token-validity-seconds}")
  private int rememberMeTokenValiditySeconds;

  @Bean
  public SecurityFilterChain filterChain(
          HttpSecurity http,
          LoginSuccessHandler loginSuccessHandler,
          LoginFailureHandler loginFailureHandler,
          RestAuthenticationEntryPoint authenticationEntryPoint,
          RestAccessDeniedHandler accessDeniedHandler,
          SessionRegistry sessionRegistry,
          UserDetailsService userDetailsService
  ) throws Exception {
    return http
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )
            .formLogin(login -> login
                    .loginProcessingUrl("/api/auth/login")
                    .successHandler(loginSuccessHandler)
                    .failureHandler(loginFailureHandler)
            )
            .logout(logout -> logout
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler(
                            new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )
            .rememberMe(rememberMe -> rememberMe
                    .key(rememberMeKey)
                    .tokenValiditySeconds(rememberMeTokenValiditySeconds)
                    .userDetailsService(userDetailsService)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout")
                    .permitAll()
                    .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                    .sessionConcurrency(concurrency -> concurrency
                            .maximumSessions(1)
                            .sessionRegistry(sessionRegistry)
                    )
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )
            .build();
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  /**
   * HttpSession 만료 시 SessionRegistry의 SessionInformation도 함께 만료시키기 위해 필요하다.
   */
  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 권한 계층: ADMIN > CHANNEL_MANAGER > USER
   */
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
            .role(Role.ADMIN.name()).implies(Role.CHANNEL_MANAGER.name())
            .role(Role.CHANNEL_MANAGER.name()).implies(Role.USER.name())
            .build();
  }

  /**
   * Method Security(@PreAuthorize)에서도 RoleHierarchy가 적용되도록 한다.
   * static 메서드여야 Bean 초기화 순서 문제가 발생하지 않는다.
   */
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
          RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }
}
