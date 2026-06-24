package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping(value = "/public",method = RequestMethod.POST)
    public ChannelResponse createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        return channelService.createPublicChannel(request);
    }

    @RequestMapping(value = "/private",method = RequestMethod.POST)
    public ChannelResponse createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        return channelService.createPrivateChannel(request);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ChannelResponse update(@PathVariable UUID id, @RequestBody ChannelUpdateRequest request) {
        return channelService.update(id, request);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        channelService.delete(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponse> findAll(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }

}
