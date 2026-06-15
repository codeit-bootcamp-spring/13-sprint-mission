package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.CreatePrivateChannelInput;
import com.sprint.mission.discodeit.dto.input.CreatePublicChannelInput;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/channel","/api/v1/channel"})
public class ChannelController {

    private ChannelService cns;

    @RequestMapping(value = "/create/private",method = RequestMethod.POST)
    public void createPrivate(
            @RequestBody CreatePrivateChannelInput cpi
    ){
        cns.createPrivateChannel(cpi);
    }

    @RequestMapping(value = "/create/public",method = RequestMethod.POST)
    public void createPublic(
            @RequestBody CreatePublicChannelInput cpi
    ){
        cns.createPublicChannel(cpi);
    }

    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    public void delete(
            @RequestBody String id
    ){
        cns.deleteChannel(UUID.fromString(id));
    }

    @RequestMapping(value = "/byUser",method = RequestMethod.POST)
    public List<ChannelOutput> findByUser(
            String userID
    ){
        return cns.findAllByUserID(UUID.fromString(userID));
    }

}
