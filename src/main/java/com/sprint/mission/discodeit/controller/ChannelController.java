package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.ChannelControllerDocs;
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
public class ChannelController implements ChannelControllerDocs {

    private final ChannelService channelService;

    @PostMapping( "/public")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PublicChannelRequest publicRequest){
        ChannelResponse publicChannel = channelService.createPublicChannel(publicRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicChannel);
    }

    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PrivateChannelRequest privateRequest){
        ChannelResponse privateChannel = channelService.createPrivateChannel(privateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(privateChannel);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable UUID channelId,
                                                         @RequestBody ChannelUpdateRequest request){
        ChannelResponse channelResponse = channelService.updateChannel(channelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(channelResponse);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId){
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<ChannelResponse>> findAllChannel(@RequestParam UUID userId){
        List<ChannelResponse> allByUserId = channelService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);
    }
}
