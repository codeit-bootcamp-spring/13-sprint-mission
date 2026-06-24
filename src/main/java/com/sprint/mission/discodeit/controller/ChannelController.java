package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Channel", description = "Channel API")
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @Operation(summary = "Public 채널 생성 API")
    @PostMapping( "/public")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PublicChannelRequest publicRequest){
        ChannelResponse publicChannel = channelService.createPublicChannel(publicRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicChannel);
    }

    @Operation(summary = "Private 채널 생성 API")
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PrivateChannelRequest privateRequest){
        ChannelResponse privateChannel = channelService.createPrivateChannel(privateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(privateChannel);
    }

    @Operation(summary = "Public 채널 수정 API")
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable UUID channelId,
                                                         @RequestBody ChannelUpdateRequest request){
        ChannelResponse channelResponse = channelService.updateChannel(channelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(channelResponse);
    }

    @Operation(summary = "채널 삭제 API")
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId){
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 사용자 채널 목록 조회 API")
    @GetMapping()
    public ResponseEntity<List<ChannelResponse>> findAllChannel(@RequestParam UUID userId){
        List<ChannelResponse> allByUserId = channelService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);
    }
}
