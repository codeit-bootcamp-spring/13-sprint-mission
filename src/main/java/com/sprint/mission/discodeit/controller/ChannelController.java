package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel")
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping (method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPublic (@RequestBody PublicChannelCreateRequest request) {
        return ResponseEntity.ok(channelService.createPublic(request));
    }

    @RequestMapping (value = "/private", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPrivate(@RequestBody PrivateChannelCreateRequest request) {
        return ResponseEntity.ok(channelService.createPrivate(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<ChannelResponse>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<ChannelResponse> update(
            @RequestBody ChannelUpdateRequest request
    ) {
        return ResponseEntity.ok(channelService.update(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }



}
