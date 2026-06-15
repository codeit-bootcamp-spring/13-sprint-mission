package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.CreateUserInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserStatusInput;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.basic.BasicUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/user","/api/v1/user"})
public class UserController {

    private final BasicUserService bus;
    private final BasicUserStatusService buss;

    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    public void regist(
            @RequestBody CreateUserInput cui
    ) {
        this.bus.createUser(cui);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public void update(
            @RequestBody UpdateUserInput uui
    ){
        this.bus.update(uui);
    }

    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    public void delete(
            @RequestBody String id
    ){
        this.bus.delete(UUID.fromString(id));
    }


    @RequestMapping(value = "findAll",method = RequestMethod.GET)
    public List<UserOutput> allUsers(){
        return this.bus.getUserList();
    }


    @RequestMapping(value = "/state", method = RequestMethod.GET)
    public void userState(){
            buss.updateByUserID(UpdateUserStatusInput.builder()
                            .lastLoginTime(Instant.now())
                            .build());
    }


}
