package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.ChannelControllerDocs;
import com.sprint.mission.discodeit.dto.channel.*;
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
    public ResponseEntity<ChannelDto> createChannel(@RequestBody PublicChannelRequest publicRequest){
        ChannelDto publicChannel = channelService.createPublicChannel(publicRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicChannel);
    }

    @PostMapping("/private")
    public ResponseEntity<ChannelDto> createChannel(@RequestBody PrivateChannelRequest privateRequest){
        ChannelDto privateChannel = channelService.createPrivateChannel(privateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(privateChannel);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelDto> updateChannel(@PathVariable UUID channelId,
                                                         @RequestBody ChannelUpdateRequest request){
        ChannelDto channelResponse = channelService.updateChannel(channelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(channelResponse);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId){
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<ChannelDto>> findAllChannel(@RequestParam UUID userId){
        List<ChannelDto> allByUserId = channelService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);
    }
}
