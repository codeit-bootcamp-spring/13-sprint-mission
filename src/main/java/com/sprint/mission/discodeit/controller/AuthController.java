package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.AuthControllerDoc;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/auth"})
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<UserDto> login(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken){  // HandlerMethodArgumentResolver 를 통해 자동으로 주입
        String tokenValue = csrfToken.getToken();
        log.debug("토큰 요청됨 - {}",tokenValue);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
