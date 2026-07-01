package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus", description = "Message 읽음 상태 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/readStatuses")
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  // GET /api/readStatuses?userId= -> User의 Message 읽음 상태 목록 조회
  @GetMapping
  public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam("userId") UUID userId) {
    List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(readStatuses);
  }

  // POST /api/readStatuses -> Message 읽음 상태 생성
  @PostMapping
  public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request) {
    ReadStatus createdReadStatus = readStatusService.create(request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdReadStatus);
  }

  // PATCH /api/readStatuses/{readStatusId} -> Message 읽음 상태 수정
  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatus> update(
          @PathVariable("readStatusId") UUID readStatusId,
          @RequestBody ReadStatusUpdateRequest request
  ) {
    ReadStatus updatedReadStatus = readStatusService.update(readStatusId, request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedReadStatus);
  }
}