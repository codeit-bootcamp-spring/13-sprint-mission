package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping (value = "/public", method = RequestMethod.POST)
    public ResponseEntity<ChannelDto> createPublic (
            @RequestBody PublicChannelCreateRequest request) {
        log.info("Received public channel create request: name={}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPublic(request));
    }

    @RequestMapping (value = "/private", method = RequestMethod.POST)
    public ResponseEntity<ChannelDto> createPrivate(
            @RequestBody PrivateChannelCreateRequest request) {
        log.info("Received private channel create request: participantCount={}", request.participantIds().size());
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPrivate(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<ChannelDto>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
    public ResponseEntity<ChannelDto> find(
            @PathVariable UUID channelId
    ){
        return ResponseEntity.ok(channelService.findById(channelId));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ResponseEntity<ChannelDto> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request
    ) {
        log.info("Received channel update request: channelId={}", channelId);
        return ResponseEntity.ok(channelService.update(channelId, request));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    ) {
        log.info("Received channel delete request: channelId={}", channelId);
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }



}
