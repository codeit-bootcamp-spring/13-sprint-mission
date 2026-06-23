package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.*;
import com.sprint.mission.discodeit.dto.output.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/channels"})
public class ChannelController {

    private final ChannelService cns;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<ChannelDto>> findAll(
            @RequestParam(value = "userId") UUID userId
    ){
        return ResponseEntity.ok(cns.findAllByUserID(userId));
    }

    @RequestMapping(value = "/{channelId}",method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    ){
        cns.deleteChannel(channelId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @RequestMapping(value = "/{channelId}",method = RequestMethod.PATCH)
    public ResponseEntity<Channel> update(
            @PathVariable UUID channelId,
            @RequestBody PublicChannelUpdateRequest pcur
    ){
        Channel res = cns.update(channelId, pcur);
        return ResponseEntity.ok(res);
    }


    @RequestMapping(value = "/private",method = RequestMethod.POST)
    public ResponseEntity<Channel> createPrivate(
            @RequestBody PrivateChannelCreateRequest cpi
    ){
        Channel res = cns.createPrivateChannel(cpi);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/public",method = RequestMethod.POST)
    public ResponseEntity<Channel> createPublic(
            @RequestBody PublicChannelCreateRequest cpi
    ){
        Channel res = cns.createPublicChannel(cpi);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
