package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    //로그인 api
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest) {
        UserResponse login = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(login);
    }




}
