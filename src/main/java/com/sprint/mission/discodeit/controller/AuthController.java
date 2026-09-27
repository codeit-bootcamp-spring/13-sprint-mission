package com.sprint.mission.discodeit.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

@RequestMapping("/api/auth")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;
    private  final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refreshToken(
            @CookieValue(
                    name = REFRESH_TOKEN_COOKIE,
                    required = false
            ) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "리프레시 토큰이 없습니다."
            );
        }

        if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(
                refreshToken
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "등록되지 않았거나 무효화된 리프레시 토큰입니다."
            );
        }

        try {
            JWTClaimsSet claims =
                    jwtTokenProvider.parseRefreshToken(refreshToken);

            UUID tokenUserId =
                    jwtTokenProvider.getUserId(claims);

            String username =
                    jwtTokenProvider.getUsername(claims);

            DiscodeitUserDetails userDetails =
                    (DiscodeitUserDetails)
                            userDetailsService.loadUserByUsername(username);

            UserDto userDto = userDetails.getUserDto();

            if (!tokenUserId.equals(userDto.id())) {
                throw new IllegalArgumentException(
                        "JWT 사용자 정보가 일치하지 않습니다."
                );
            }

            String newAccessToken =
                    jwtTokenProvider.refreshAccessToken(
                            refreshToken,
                            userDto.role()
                    );

            String newRefreshToken =
                    jwtTokenProvider.createRefreshToken(
                            userDto.id(),
                            userDto.username()
                    );

            JwtInformation newJwtInformation =
                    new JwtInformation(
                            userDto,
                            newAccessToken,
                            newRefreshToken
                    );

            jwtRegistry.rotateJwtInformation(
                    refreshToken,
                    newJwtInformation
            );

            ResponseCookie refreshTokenCookie =
                    ResponseCookie
                            .from(
                                    REFRESH_TOKEN_COOKIE,
                                    newRefreshToken
                            )
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .sameSite("Lax")
                            .maxAge(Duration.ofSeconds(
                                    jwtTokenProvider
                                            .getRefreshTokenExpirationTime()
                            ))
                            .build();

            JwtDto response =
                    new JwtDto(userDto, newAccessToken);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.SET_COOKIE,
                            refreshTokenCookie.toString()
                    )
                    .body(response);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "유효하지 않은 리프레시 토큰입니다.",
                    e
            );
        }
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {

        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity
                .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
                .build();
    }


    @PutMapping("/role")
    public ResponseEntity<UserDto> updateRole (
            @Valid @RequestBody UserRoleUpdateRequest request
            ) {
        UserDto updateUser = authService.updateRole(
                request.userId(),
                request.newRole()
        );

        return ResponseEntity.ok(updateUser);
    }
}

