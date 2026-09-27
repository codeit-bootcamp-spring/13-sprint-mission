package com.sprint.mission.discodeit.filter;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.role.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
user and password filter 앞에 위치한다. 로그인이 죄어있는지 확인하고, 로그인 로직으로 넘겨야 인증을 덮어쓰지 않기 때문.
유저 정보를 확인해서, jwt 토큰 관련 로직을 수행하고, Authentication 객체를 요청(Request) 에 주입한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String BEARER_PREFIX = "bearer";

    /** 엔트리 포인트에 전달할 플래그 */
    public static final String ATTR_JWT_ERROR = "jwt.error";
    public static final String ERROR_EXPIRED = "expired";
    public static final String ERROR_INVALID = "invalid";

    private final JwtTokenProvider provider;
    private final DiscodeitUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);   // AccessToken(Bearer)

        if (token != null) {
            try {
                Claims claims = provider.parseClaims(token);
                String username = claims.getSubject();
                Role role = provider.getRole(claims);



                // principal 은 인가를 확인할 때 필요. -> UserDetail
                // todo - id 로 변경된다면 기준 변경.
                UserDetails principal = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        getAuthority(role)
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 현재 스레드의 SecurityContext 에 인증정보 추가.
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (ExpiredJwtException e) {
                request.setAttribute(ATTR_JWT_ERROR, ERROR_EXPIRED);
                log.debug("[JWT] 만료된 토큰으로 접근 - {}", e.getMessage());
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ATTR_JWT_ERROR, ERROR_INVALID);
                log.debug("[JWT] 유효하지 않은 토큰으로 접근 - {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /*
    유틸리티 매서드
     */
    private String resolveToken(HttpServletRequest request){
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header != null && header.startsWith(BEARER_PREFIX))
            return header.substring(BEARER_PREFIX.length());
        return null;
    }

    private List<SimpleGrantedAuthority> getAuthority(Role role){
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }



}
