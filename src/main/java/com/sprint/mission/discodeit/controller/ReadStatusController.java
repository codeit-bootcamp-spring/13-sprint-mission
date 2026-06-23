package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.CreateReadStatusRequest;
import com.sprint.mission.discodeit.dto.request.UpdateReadStatusRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusResponse create(@RequestBody CreateReadStatusRequest request) {
        return readStatusService.create(request);
    }

    // 메시지 수신 정보 수정
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PUT)
    public ReadStatusResponse update(@PathVariable UUID userId, @PathVariable UUID channelId, @RequestBody UpdateReadStatusRequest request) {
        return readStatusService.updateLastReadAt(userId, channelId);
    }

    // 메시지 수신 정보 조회
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public List<ReadStatusResponse> get(@PathVariable UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }

}
