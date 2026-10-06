package com.sprint.mission.discodeit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    private final JwtRegistry jwtRegistry;

    private final DiscodeitUserDetailsService
            discodeitUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token =
                resolveToken(request);

        if (
                token != null
                        && jwtTokenProvider
                        .validateAccessToken(token)
                        && jwtRegistry
                        .hasActiveJwtInformationByAccessToken(
                                token
                        )
                        && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null
        ) {

            String username =
                    jwtTokenProvider
                            .getUsername(token);

            UserDetails userDetails =
                    discodeitUserDetailsService
                            .loadUserByUsername(
                                    username
                            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private String resolveToken(
            HttpServletRequest request
    ) {

        String authorizationHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith(
                        BEARER_PREFIX
                )
        ) {

            return null;
        }

        String token =
                authorizationHeader.substring(
                        BEARER_PREFIX.length()
                );

        if (token.isBlank()) {
            return null;
        }

        return token;
    }
}