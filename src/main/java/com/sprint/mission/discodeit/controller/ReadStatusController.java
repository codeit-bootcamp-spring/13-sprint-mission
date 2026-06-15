package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("read-statuses")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 1. (특정 채널의) 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusResponse createReadStatus
    (@RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    // 2. (특정 채널의) 메시지 수신 정보 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ReadStatusResponse updateReadStatus
    (@PathVariable UUID id, @RequestBody ReadStatusUpdateRequest request) {
        return readStatusService.update(id, request);
    }

    // 3. (특정 사용자의) 메세지 수신 정보 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<ReadStatusResponse> getReadStatus(@RequestParam UUID id) {
        return readStatusService.findAllByUserId(id);
    }

}
