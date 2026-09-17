package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.AuthControllerDoc;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/auth"})
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;

    @GetMapping("csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken){  // HandlerMethodArgumentResolver 를 통해 자동으로 주입
        String tokenValue = csrfToken.getToken();
        log.debug("토큰 요청됨 - {}",tokenValue);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("me")
    public ResponseEntity<UserDto> me(
            @AuthenticationPrincipal DiscodeitUserDetails userDetails   // 인증이 성공한다면, 자동으로 반환.
    ){
        return ResponseEntity.ok(userDetails.getUserDto());
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
}
