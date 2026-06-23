package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    // 공개 채널 생성
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelResponse createPublicChannel(@RequestBody CreatePublicChannelRequest request) {
        return channelService.createPublicChannel(request);
    }

    // 비공개 채널 생성
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelResponse createPrivateChannel(@RequestBody CreatePrivateChannelRequest request) {
        return channelService.createPrivateChannel(request);
    }

    // 공개 채널 정보 수정
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PUT)
    public ChannelResponse update(@PathVariable UUID id,
                                  @RequestBody UpdateChannelRequest request) {
        return channelService.update(id, request);
    }

    // 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        channelService.delete(id);
    }

    // 모든 채널 목록 조회
    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
    public List<ChannelResponse> findAllByUserId(@PathVariable UUID userId) {
        return channelService.findAllByUserId(userId);
    }

}
