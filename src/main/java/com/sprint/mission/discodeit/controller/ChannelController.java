package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
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

    @PostMapping
    public ChannelDto create(
            @RequestBody CreateChannelRequest request
    ) {
        return channelService.create(request);
    }

    @GetMapping
    public List<ChannelDto> findAll() {
        return channelService.findAll();
    }

    @GetMapping("/{id}")
    public ChannelDto find(
            @PathVariable UUID id
    ) {
        return channelService.find(id);
    }

    @PutMapping("/{id}")
    public ChannelDto update(
            @PathVariable UUID id,
            @RequestBody UpdateChannelRequest request
    ) {
        return channelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        channelService.delete(id);
    }
}