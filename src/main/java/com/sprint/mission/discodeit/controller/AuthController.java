package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.AuthControllerDoc;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/auth"})
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken){  // HandlerMethodArgumentResolver 를 통해 자동으로 주입
        String tokenValue = csrfToken.getToken();

        log.debug("토큰 요청됨 - {}",tokenValue);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> changeRole(
            @RequestBody UserRoleUpdateRequest request
    ){
        return ResponseEntity.ok(
                authService.roleUpdate(request.userId(), request.newRole())
        );
    }



    // todo - 전용 에러 + handler 로 401 에러 반환.
    @PostMapping("refresh")
    public ResponseEntity<JwtDto> refresh(
            @CookieValue(value = "REFRESH_TOKEN", required = false) String token,
            HttpServletResponse response
    ){
        JwtInformation output = authService.jwtRefresh(token);

        response.addCookie(refreshCookie(output.getRefreshToken()));

        return ResponseEntity.ok()
                .body(new JwtDto(
                        output.getUserDto(),
                        output.getAccessToken()
                ));
    }

    private Cookie refreshCookie(String token){
        // refresh token 쿠키 세팅
        Cookie cookie = new Cookie("REFRESH_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtTokenProvider.refreshCookieExpire());

        return cookie;
    }
}
