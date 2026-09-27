package com.sprint.mission.discodeit.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization
                .substring(BEARER_PREFIX.length())
                .trim();

        if (accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JWTClaimsSet claims =
                    jwtTokenProvider.parseAccessToken(accessToken);

            if (!jwtRegistry.hasActiveJwtInformationByAccessToken(
                    accessToken
            )) {
                throw new IllegalArgumentException(
                        "등록되지 않았거나 무효화된 액세스 토큰입니다."
                );
            }

            String username =
                    jwtTokenProvider.getUsername(claims);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (IllegalArgumentException
                 | UsernameNotFoundException e) {

            log.warn("[JWT] 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}