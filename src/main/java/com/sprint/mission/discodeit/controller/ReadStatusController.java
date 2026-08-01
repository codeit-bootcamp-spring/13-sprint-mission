package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<ReadStatusDto> create(
      @RequestBody ReadStatusCreateRequest readStatusCreateRequest) {
    log.debug("[ReadStatus 생성 요청] userId: {}, channelId: {}",
        readStatusCreateRequest.userId(), readStatusCreateRequest.channelId());

    ReadStatusDto readStatus = readStatusService.create(readStatusCreateRequest);

    log.info("[ReadStatus 생성 완료] readStatusId: {}", readStatus.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(readStatus);
  }

  @RequestMapping(value = "/{readStatusId}", method = RequestMethod.GET)
  public ResponseEntity<ReadStatusDto> find(@PathVariable UUID readStatusId) {

    ReadStatusDto readStatus = readStatusService.find(readStatusId);

    return ResponseEntity.status(HttpStatus.OK).body(readStatus);
  }


  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId) {

    List<ReadStatusDto> findAllByUserId = readStatusService.findAllByUserId(userId);

    return ResponseEntity.status(HttpStatus.OK).body(findAllByUserId);
  }


  @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
  public ResponseEntity<ReadStatusDto> update(@PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest readStatusUpdateRequest) {
    log.debug("[ReadStatus 수정 요청] readStatusId: {}", readStatusId);

    ReadStatusDto readStatusUpdate = readStatusService.update(readStatusId,
        readStatusUpdateRequest);

    log.info("[ReadStatus 수정 완료] readStatusId: {}", readStatusUpdate.id());
    return ResponseEntity.status(HttpStatus.OK).body(readStatusUpdate);
  }


  @RequestMapping(value = "/{readStatusId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID readStatusId) {
    log.debug("[ReadStatus 삭제 요청] readStatusId: {}", readStatusId);

    readStatusService.delete(readStatusId);

    log.info("[ReadStatus 삭제 완료] readStatusId: {}", readStatusId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}