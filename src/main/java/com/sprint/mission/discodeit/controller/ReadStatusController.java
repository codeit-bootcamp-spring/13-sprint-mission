package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/read-statuses")
@RequiredArgsConstructor
@ResponseBody
public class ReadStatusController {

    private final ReadStatusService readStatusService;
    // 사용자가 채널 별 마지막으로 메시지 읽은 시간을 표현하는 도메인 모델
    // 사용자별 각 채널에 읽지 않은 메시지 확인하기 위해 활용

    // 특정 채널의 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatusResponse> create(@Valid @RequestBody ReadStatusCreateRequest request){
        ReadStatusResponse readStatus = readStatusService.create(request);
        URI location = URI.create("/api/read-statuses/" + readStatus.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatus);
    }

    // 특정 채널의 메시지 수신 정보 수정
    @RequestMapping(path = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatusResponse> update(@PathVariable UUID readStatusId,
                                                     @Valid @RequestBody ReadStatusUpdateRequest request){
        ReadStatusResponse updatedReadStatus = readStatusService.update(readStatusId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedReadStatus);
    }

    // 특정 사용자의 메시지 수신 정보 조회
    @RequestMapping(path = "/users/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(@PathVariable UUID userId){
        List<ReadStatusResponse> foundAllByUserId = readStatusService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(foundAllByUserId);
    }
}
