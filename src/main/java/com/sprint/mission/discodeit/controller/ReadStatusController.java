package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @PostMapping
  public ResponseEntity<ReadStatusDto> create(
      @RequestBody @Valid ReadStatusCreateRequest request
  ) {
    log.debug(
        "읽음 상태 생성 요청: userId={}, channelId={}",
        request.userId(),
        request.channelId()
    );

    ReadStatusDto response = readStatusService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> updateLastReadAt(
      @PathVariable("readStatusId") UUID id,
      @RequestBody @Valid ReadStatusUpdateRequest request
  ) {
    log.debug("읽음 상태 수정 요청: readStatusId={}", id);

    ReadStatusDto response =
        readStatusService.updateLastReadAt(id, request);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(
      @RequestParam("userId") UUID userId
  ) {
    log.debug("사용자별 읽음 상태 조회 요청: userId={}", userId);

    List<ReadStatusDto> response =
        readStatusService.findAllByUserId(userId);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> findById(
      @PathVariable("readStatusId") UUID id
  ) {
    log.debug("읽음 상태 단건 조회 요청: readStatusId={}", id);

    ReadStatusDto response = readStatusService.findById(id);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{readStatusId}")
  public ResponseEntity<Void> delete(
      @PathVariable("readStatusId") UUID id
  ) {
    log.debug("읽음 상태 삭제 요청: readStatusId={}", id);

    readStatusService.delete(id);

    return ResponseEntity.noContent().build();
  }
}
