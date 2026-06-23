package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.*;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.basic.BasicReadStatusService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.basic.BasicUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/user","/api/v1/user"})
public class UserController {

    private final BasicUserService bus;
    private final BasicUserStatusService buss;
    private final BasicReadStatusService brss;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void regist(
            @RequestBody CreateUserInput cui
    ) {
        this.bus.createUser(cui);
    }

    @RequestMapping(value = "/", method = RequestMethod.PATCH)
    public void update(
            @RequestBody UpdateUserInput uui
    ){
        this.bus.update(uui);
    }

    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    public void delete(
            @RequestBody IDRequestInput id
    ){
        this.bus.delete(id.getID());
    }


    @RequestMapping(value = "/findAll",method = RequestMethod.GET)
    public List<UserDto> allUsers(){
        return this.bus.getUserList();
    }


    @RequestMapping(value = "/state", method = RequestMethod.POST)
    public void userState(
            @RequestBody IDRequestInput id
    ){
            buss.updateByUserID(id.getID());
    }

    @RequestMapping(value = "/msgStatus", method = RequestMethod.POST)
    public List<ReadStatus> queryByUser(
            @RequestBody IDRequestInput userID
    ){
        return brss.findAllByUserID(userID.getID());
    }

}
