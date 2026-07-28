package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
@Slf4j
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody ChannelPrivateRequest channelRequest) {
        int participantCount = channelRequest.channelIds() == null
                ? 0
                : channelRequest.channelIds().size();
        log.debug("PRIVATE 채널 생성 API 요청: participantCount={}", participantCount);

        ChannelResponse response = channelService.createPrivateChannel(channelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(@RequestBody ChannelPublicRequest channelRequest) {
        log.debug("PUBLIC 채널 생성 API 요청: name={}", channelRequest.name());
        ChannelResponse response = channelService.createPublicChannel(channelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChannelResponse>> getAllChannels(@RequestParam("userId") UUID userId) {
        List<ChannelResponse> responses = channelService.findAll(userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(
            @PathVariable("channelId") UUID channelId,
            @RequestBody ChannelPublicRequest channelRequest) {
        log.debug("채널 수정 API 요청: channelId={}", channelId);
        ChannelResponse response = channelService.update(channelId, channelRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable("channelId") UUID channelId) {
        log.debug("채널 삭제 API 요청: channelId={}", channelId);
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

}
