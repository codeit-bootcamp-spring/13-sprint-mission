package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/read-statuses")
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService;

  @PostMapping
  public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request) {
    // Request DTO에서 값을 꺼내서 Service에 전달
    ReadStatus createdReadStatus = readStatusService.create(
            request.userId(),
            request.channelId(),
            request.lastReadAt()
    );
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdReadStatus);
  }

  @PatchMapping(path = "{readStatusId}")
  public ResponseEntity<ReadStatus> update(
          @PathVariable("readStatusId") UUID readStatusId,
          @RequestBody ReadStatusUpdateRequest request) {
    // Request DTO에서 값을 꺼내서 Service에 전달
    ReadStatus updatedReadStatus = readStatusService.update(
            readStatusId,
            request.newLastReadAt()
    );
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedReadStatus);
  }

  @GetMapping
  public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam("userId") UUID userId) {
    List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(readStatuses);
  }
}