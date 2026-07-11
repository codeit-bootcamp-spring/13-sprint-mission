package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping (value = "/public", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPublic (
            @RequestBody PublicChannelCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPublic(request));
    }

    @RequestMapping (value = "/private", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPrivate(
            @RequestBody PrivateChannelCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPrivate(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<ChannelResponse>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
    public ResponseEntity<ChannelResponse> find(
            @PathVariable UUID channelId
    ){
        return ResponseEntity.ok(channelService.findById(channelId));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ResponseEntity<ChannelResponse> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request
    ) {
        return ResponseEntity.ok(channelService.update(channelId, request));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    ) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }



}
