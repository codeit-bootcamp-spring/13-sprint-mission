package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.AuthControllerDoc;
import com.sprint.mission.discodeit.dto.input.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/auth"})
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<User> login(
            @RequestBody LoginRequest loginRequest
    ){
        return ResponseEntity.ok(authService.login(loginRequest));
    }



}
