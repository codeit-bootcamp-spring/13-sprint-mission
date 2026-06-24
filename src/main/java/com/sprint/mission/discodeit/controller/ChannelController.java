package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.dto.response.ChannelUpdateResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel")
public class ChannelController {

    private final ChannelService channelService;

    //비공개 채널 생성
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ResponseEntity<Void> createPrivateChannel(@Valid @RequestBody PrivateChannelCreateRequest request) {
        channelService.createPrivateChannel(request);

        return ResponseEntity.ok().build();
    }

    //공개 채널 생성
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity<Void> createPublicChannel(@Valid @RequestBody PublicChannelCreateRequest request) {
        channelService.createPublicChannel(request);

        return ResponseEntity.ok().build();
    }

    //특정 사용자가 볼 수 있는 모든 채널 목록 조회
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<ChannelFindResponse>> findChannelsByUser(@PathVariable UUID userId) {
        List<ChannelFindResponse> responseList = channelService.findAllByUserId(userId);

        return ResponseEntity.ok().body(responseList);
    }

    //공개 채널의 정보 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ChannelUpdateResponse> updateChannel(@PathVariable UUID id, @Valid @RequestBody ChannelUpdateRequest request) {
        ChannelUpdateResponse response = channelService.updateChannel(request);

        return ResponseEntity.ok().body(response);
    }

    //채널 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        channelService.deleteChannel(id);

        return ResponseEntity.noContent().build();
    }

}
