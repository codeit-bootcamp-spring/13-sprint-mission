package com.sprint.mission.discodeit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtRegistry jwtRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       try{
           processJwtAuthentication(request);
       }catch (AuthenticationException e){
           SecurityContextHolder.clearContext();
           restAuthenticationEntryPoint.commence(request, response, e);
           return;
       }

        filterChain.doFilter(request, response);
    }

    private void processJwtAuthentication(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null){
            log.warn("Authorization 헤더가 없어 JWT 인증을 건너뜁니다.");
            return ;
        }
        if (!header.startsWith(BEARER_PREFIX)){
            log.warn("Authorization 헤더가 Bearer 방식이 아니므로 JWT 인증을 건너뜁니다.");
            return ;
        }

        String accessToken = header.substring(BEARER_PREFIX.length());
        jwtTokenProvider.validateToken(accessToken)
                .filter(claims -> accessTokenTypeName().equals(jwtTokenProvider.getTokenType(claims)))
                .filter(claims -> jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .ifPresent(claims ->{
                    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) discodeitUserDetailsService.loadUserByUsername(claims.getSubject());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

    }

    private String accessTokenTypeName() {
        return JwtTokenProvider.TokenType.ACCESS.name();
    }

    /**
     * {@code @Component} 필터가 서블릿 컨테이너에도 자동 등록되는 것을 방지한다.
     * 동일한 필터 인스턴스(this)의 서블릿 직접 등록만 비활성화하고,
     * SecurityConfig의 addFilterBefore로 등록한 Security 체인에서는 계속 실행한다.
     * 다른 서블릿 필터와 Security 필터의 등록은 변경하지 않는다.
     */
    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> discodeitAuthenticationFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(this);
        registration.setEnabled(false);
        return registration;
    }
}
