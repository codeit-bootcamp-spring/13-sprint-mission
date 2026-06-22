package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.auth.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 권한 관리
    // 사용자 로그인 가능
    @RequestMapping(value = "/login", method = RequestMethod.POST) // 비밀번호 같은 민감한 정보는 숨겨서 전달한다
    public ResponseEntity<UserResponse> login(@Valid @RequestBody AuthLoginRequest request){
        UserResponse login = authService.login(request);
        return ResponseEntity.ok().body(login);
    }
}
