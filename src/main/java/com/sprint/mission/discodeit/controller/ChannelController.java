package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.ChannelControllerDoc;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
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
public class ChannelController implements ChannelControllerDoc {

    private final ChannelService channelService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<ChannelDto>> findAll(
            @RequestParam(value = "userId") UUID userId
    ){
        return ResponseEntity.ok(channelService.findAllByUserID(userId));
    }

    @RequestMapping(value = "/{channelId}",method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    ){
        channelService.deleteChannel(channelId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @RequestMapping(value = "/{channelId}",method = RequestMethod.PATCH)
    public ResponseEntity<ChannelDto> update(
            @PathVariable UUID channelId,
            @Valid @RequestBody PublicChannelUpdateRequest pcur
    ){
        ChannelDto res = channelService.update(channelId, pcur);
        return ResponseEntity.ok(res);
    }


    @RequestMapping(value = "/private",method = RequestMethod.POST)
    public ResponseEntity<ChannelDto> createPrivate(
            @Valid @RequestBody PrivateChannelCreateRequest cpi
    ){
        ChannelDto res = channelService.createPrivateChannel(cpi);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/public",method = RequestMethod.POST)
    public ResponseEntity<ChannelDto> createPublic(
            @Valid @RequestBody PublicChannelCreateRequest cpi
    ){
        ChannelDto res = channelService.createPublicChannel(cpi);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
