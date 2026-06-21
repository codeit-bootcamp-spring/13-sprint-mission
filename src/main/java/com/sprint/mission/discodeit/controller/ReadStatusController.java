package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/read-statuses")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 1. 특정 채널의 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ReadStatus createReadStatus(@RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    // 2. 특정 채널의 메시지 수신 정보 수정 (최근 읽은 시간 업데이트)
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PUT)
    public ReadStatus updateReadStatus(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request) {
        return readStatusService.update(readStatusId, request);
    }

    // 3. 특정 사용자의 메시지 수신 정보 목록 조회 (쿼리 파라미터 ?userId=... 사용)
    @RequestMapping(method = RequestMethod.GET)
    public List<ReadStatus> getReadStatusesByUserId(@RequestParam UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}