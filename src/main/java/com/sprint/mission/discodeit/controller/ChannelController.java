package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    // 공개 채널 생성
    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        ChannelResponse response = channelService.createPublicChannel(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 비공개 채널 생성
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        ChannelResponse response = channelService.createPrivateChannel(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 공개 채널 정보 수정
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request) {

        ChannelResponse response = channelService.update(channelId, request);

        return ResponseEntity.ok(response);
    }

    // 삭제
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);

        return ResponseEntity.noContent().build();
    }

    // 모든 채널 목록 조회
    @GetMapping
    public ResponseEntity<List<ChannelResponse>> findAllByUserId(@RequestParam UUID userId) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

}
