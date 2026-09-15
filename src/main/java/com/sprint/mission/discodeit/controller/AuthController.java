package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {


    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken){
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);
        return ResponseEntity.status(203).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal DiscodeitUserDetails userDetails){
        return ResponseEntity.ok(userDetails.getUserDto());
    }
}
