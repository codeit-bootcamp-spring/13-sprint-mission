package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    // 1. 채널 생성
    // 비공개 채널
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelResponse createChannel(@RequestBody ChannelPrivateRequest channelRequest) {
        return channelService.createPrivateChannel(channelRequest);
    }

    // 공개 채널
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelResponse createChannel(@RequestBody ChannelPublicRequest channelRequest) {
        return channelService.createPublicChannel(channelRequest);
    }

    // 2. 특정 채널 단건 조회
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ChannelResponse getChannel(@PathVariable UUID id) {
        return channelService.findById(id)
                .orElseThrow(() -> new DiscodeitException.ChannelNotFoundException
                        ("해당 채널을 찾을 수 없습니다."));
    }

    // 3. 전체 채널 조회
    // [ ] 특정 사용자가 볼 수 있는 모든 채널 목록을 조회할 수 있다.
    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponse> getAllChannels (@RequestParam UUID userId) {
        return channelService.findAll(userId);
    }

    // 4. 채널 이름 수정 (공개 채널만 가능)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ChannelResponse updateChannel
    (@PathVariable UUID id, @RequestBody ChannelPublicRequest channelRequest) {
        return channelService.update(id, channelRequest);
    }

    // 5. 채널 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteChannel(@PathVariable UUID id) {
        channelService.delete(id);
    }

}
