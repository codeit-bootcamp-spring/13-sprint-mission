package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping("/public")
    public ChannelResponse publicCreate(
            @RequestBody ChannelRequest.CreatePublicChannel request
    ) {
        return channelService.createPublicChannel(request);
    }

    @PostMapping("/private")
    public ChannelResponse privateCreate(
            @RequestBody ChannelRequest.CreatePrivateChannel request
    ) {
        return channelService.createPrivateChannel(request);
    }

    @PatchMapping("/{channelId}")
    public ChannelResponse update(@PathVariable UUID channelId,
                                  @RequestBody ChannelRequest.UpdateChannel request){
        return channelService.update(channelId, request);
    }

   @DeleteMapping("/{channelId}")
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }

    @GetMapping("/user/{userId}")
    public List<ChannelResponse> findAllByUserId(
            @PathVariable UUID userId
    ) {
        return channelService.findAllByUserId(userId);
    }
}

