package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping(method = RequestMethod.POST)
    public Channel create(
            @RequestBody CreateChannelRequest request
    ) {
        return channelService.create(
                new Channel(
                        request.getName(),
                        request.getDescription()
                )
        );
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Channel> findAll() {
        return channelService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Channel find(@PathVariable UUID id) {
        return channelService.find(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Channel update(
            @PathVariable UUID id,
            @RequestBody UpdateChannelRequest request
    ) {
        return channelService.update(
                id,
                request.getName(),
                request.getDescription()
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        channelService.delete(id);
    }
}