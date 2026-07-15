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
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public Channel create(@RequestBody CreateChannelRequest request) {
        return channelService.create(
                new Channel(
                        request.getName(),
                        request.getDescription()
                )
        );
    }

    @GetMapping
    public List<Channel> findAll() {
        return channelService.findAll();
    }

    @GetMapping("/{id}")
    public Channel find(@PathVariable UUID id) {
        return channelService.find(id);
    }

    @PatchMapping("/{id}")
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        channelService.delete(id);
    }
}