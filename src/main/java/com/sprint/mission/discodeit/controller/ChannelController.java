package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController { // 채널 관리

    private final ChannelService channelService;

    // 공개 채널 생성
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPublicChannel(@Valid @RequestBody PublicChannelCreateRequest request) {
        ChannelResponse publicChannel = channelService.createPublicChannel(request);
        URI location = URI.create("/api/channels/"+publicChannel.getId());
        return ResponseEntity.created(location).body(publicChannel);
    }

    // 비공개 채널 생성
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPrivateChannel(@Valid @RequestBody PrivateChannelCreateRequest request) {
        ChannelResponse privateChannel = channelService.createPrivateChannel(request);
        URI location = URI.create("/api/channels/"+privateChannel.getId());
        return ResponseEntity.created(location).body(privateChannel);
    }

    // 공개 채널 정보 수정
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ResponseEntity<ChannelResponse> updatePublicChannel(@PathVariable UUID channelId,
                                                               @Valid @RequestBody ChannelUpdateRequest request) {
        ChannelResponse updatedChannel = channelService.update(channelId, request);
        return ResponseEntity.ok().body(updatedChannel);
    }

    // 채널 삭제
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

    // 특정 사용자가 볼 수 있는 채널목록 조회
    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<ChannelResponse>> findAllByUserId(@PathVariable UUID userId) {
        List<ChannelResponse> foundChannels = channelService.findAllByUserId(userId);
        return ResponseEntity.ok().body(foundChannels);
    }
}
