package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 메시지 수신 정보 생성
    @PostMapping
    public ResponseEntity<ReadStatusResponse> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatusResponse response = readStatusService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 메시지 수신 정보 수정
    @PatchMapping("/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request) {
        ReadStatusResponse response = readStatusService.update(readStatusId, request);

        return ResponseEntity.ok(response);
    }

    // 메시지 수신 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<ReadStatusResponse>> get(@RequestParam UUID userId) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }

}
