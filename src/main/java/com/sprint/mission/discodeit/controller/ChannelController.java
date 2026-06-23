package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.*;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.ReadStatusService;
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
@RequestMapping({"/api/channel","/api/v1/channel"})
public class ChannelController {

    private final ChannelService cns;
    private final ReadStatusService rss;

    @RequestMapping(value = "/private",method = RequestMethod.POST)
    public void createPrivate(
            @RequestBody CreatePrivateChannelInput cpi
    ){
        cns.createPrivateChannel(cpi);
    }

    @RequestMapping(value = "/public",method = RequestMethod.POST)
    public void createPublic(
            @RequestBody CreatePublicChannelInput cpi
    ){
        cns.createPublicChannel(cpi);
    }

    @RequestMapping(value = "/public",method = RequestMethod.PATCH)
    public void updatePBChannel(
            @RequestBody UpdateChannelInput uci
    ){
        cns.updateChannelInfo(uci);
    }

    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    public void delete(
            @RequestBody IDRequestInput id
    ){
        cns.deleteChannel(id.getID());
    }

    @RequestMapping(value = "/byUser",method = RequestMethod.POST)
    public List<ChannelOutput> findByUser(
            @RequestBody IDRequestInput userID
    ){
        return cns.findAllByUserID(userID.getID());
    }

    @RequestMapping(value = "/msgStatus",method = RequestMethod.POST)
    public void createChannelState(
            @RequestBody CreateReadyStatusInput crsi
    ){
        rss.create(crsi);
    }

    @RequestMapping(value = "/msgStatus",method = RequestMethod.PATCH)
    public void updateChannelState(
            @RequestBody UpdateReadStatusInput ursi
    ){
        rss.update(ursi);
    }
}
