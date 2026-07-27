package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping("/public")
    public ResponseEntity<ChannelDto> publicCreate(
            @Valid @RequestBody CreatePublicChannelRequest request
    ) {
        ChannelDto channel = channelService.createPublicChannel(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    @PostMapping("/private")
    public ResponseEntity<ChannelDto> privateCreate(
            @Valid @RequestBody CreatePrivateChannelRequest request
    ) {
        ChannelDto channel = channelService.createPrivateChannel(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelDto> update(@PathVariable UUID channelId,
                           @Valid @RequestBody CreatePublicChannelRequest request){
        ChannelDto channel = channelService.update(channelId, request.toCommand());
        return ResponseEntity.ok(channel);
    }

   @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ChannelDto>>findAllByUserId(
            @RequestParam UUID userId
    ) {
        List<ChannelDto> channel = channelService.findAllByUserId(userId);
        return ResponseEntity.ok(channel);
    }
}

