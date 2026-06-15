package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.service.basic.BasicUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/user","/api/v1/user"})
public class UserController {

    private BasicUserService bus;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void regist(@RequestBody User user) {
    }


}
