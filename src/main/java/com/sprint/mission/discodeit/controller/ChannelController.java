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

    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelResponse publicCreate(
            @RequestBody ChannelRequest.CreatePublicChannel request
    ) {
        return channelService.createPublicChannel(request);
    }

    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelResponse privateCreate(
            @RequestBody ChannelRequest.CreatePrivateChannel request
    ) {
        return channelService.createPrivateChannel(request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ChannelResponse update(@PathVariable UUID channelId,
                                  @RequestBody ChannelRequest.UpdateChannel request){
        return channelService.update(channelId, request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }

    @RequestMapping (method = RequestMethod.GET)
    public List<ChannelResponse> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return channelService.findAllByUserId(userId);
    }
}

