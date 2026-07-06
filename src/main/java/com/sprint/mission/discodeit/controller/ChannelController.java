package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    // 1. 채널 생성
    // 비공개 채널
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody ChannelPrivateRequest channelRequest) {
        ChannelResponse response = channelService.createPrivateChannel(channelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 공개 채널
    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(@RequestBody ChannelPublicRequest channelRequest) {
        ChannelResponse response = channelService.createPublicChannel(channelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 특정 채널 단건 조회
    // [ ] 특정 사용자가 볼 수 있는 모든 채널 목록을 조회할 수 있다.
    @GetMapping
    public ResponseEntity<List<ChannelResponse>> getAllChannels(@RequestParam("userId") UUID userId) {
        List<ChannelResponse> responses = channelService.findAll(userId);
        return ResponseEntity.ok(responses);
    }

    // 3. 채널 이름 수정 (공개 채널만 가능)
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(
            @PathVariable("channelId") UUID channelId,
            @RequestBody ChannelPublicRequest channelRequest) {
        ChannelResponse response = channelService.update(channelId, channelRequest);
        return ResponseEntity.ok(response);
    }

    // 4. 채널 삭제
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable("channelId") UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

}
