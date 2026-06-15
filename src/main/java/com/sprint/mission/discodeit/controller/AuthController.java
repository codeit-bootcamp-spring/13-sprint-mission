package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.LoginInput;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/auth","/api/v1/auth"})
public class AuthController {

    private AuthService as;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(
            @RequestBody LoginInput loginInput
    ){
        as.login(loginInput);
        return "";
    }



}
