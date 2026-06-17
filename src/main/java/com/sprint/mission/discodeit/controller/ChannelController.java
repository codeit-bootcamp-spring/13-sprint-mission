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
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PublicChannelRequest publicRequest){
        ChannelResponse publicChannel = channelService.createPublicChannel(publicRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicChannel);
    }

    //비공개 채널 생성 api
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PrivateChannelRequest privateRequest){
        ChannelResponse privateChannel = channelService.createPrivateChannel(privateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(privateChannel);
    }

    //공개채널 정보 수정 api
    @RequestMapping(value = "/{channelid}", method = RequestMethod.PATCH)
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable UUID channelid,
                                                         @RequestBody ChannelUpdateRequest request){
        ChannelResponse channelResponse = channelService.updateChannel(channelid, request);
        return ResponseEntity.status(HttpStatus.OK).body(channelResponse);
    }

    //채널삭제 api
    @RequestMapping(value = "/{channelid}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelid){
        channelService.deleteChannel(channelid);
        return ResponseEntity.noContent().build();
    }

    //특정 사용자 모든 채널목록 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelResponse>> findAllChannel(@RequestParam UUID userid){
        List<ChannelResponse> allByUserId = channelService.findAllByUserId(userid);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);
    }













}
