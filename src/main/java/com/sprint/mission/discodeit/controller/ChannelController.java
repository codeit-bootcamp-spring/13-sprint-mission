package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping(value = "/public")
    public ResponseEntity<ChannelDto> createPublic(@RequestBody PublicChannelCreateRequest request) {
        return ResponseEntity.ok(channelService.createPublic(request));
    }

    @PostMapping(value = "/private")
    public ResponseEntity<ChannelDto> createPrivate(@RequestBody PrivateChannelCreateRequest request) {
        return ResponseEntity.ok(channelService.createPrivate(request));
    }

    @GetMapping
    public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam UUID userId) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @PatchMapping(value = "/{channelId}")
    public ResponseEntity<ChannelDto> update(@PathVariable UUID channelId, @RequestBody ChannelUpdateRequest request) {
        return ResponseEntity.ok(channelService.update(channelId, request));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
