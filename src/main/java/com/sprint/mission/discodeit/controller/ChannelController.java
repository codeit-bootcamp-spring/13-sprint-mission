package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
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


    //공개 채널 생성 api
    @PostMapping( "/public")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PublicChannelRequest publicRequest){
        ChannelResponse publicChannel = channelService.createPublicChannel(publicRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicChannel);
    }

    //비공개 채널 생성 api
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PrivateChannelRequest privateRequest){
        ChannelResponse privateChannel = channelService.createPrivateChannel(privateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(privateChannel);
    }

    //공개채널 정보 수정 api
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable UUID channelId,
                                                         @RequestBody ChannelUpdateRequest request){
        ChannelResponse channelResponse = channelService.updateChannel(channelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(channelResponse);
    }

    //채널삭제 api
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId){
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    //특정 사용자 모든 채널목록 조회
    @GetMapping()
    public ResponseEntity<List<ChannelResponse>> findAllChannel(@RequestParam UUID userId){
        List<ChannelResponse> allByUserId = channelService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);
    }
}
