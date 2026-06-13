package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.basic.BasicAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    //사용자 로그인
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        authService.login(request);

        return ResponseEntity.ok().build();
    }

}
