package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@RestController
@RequestMapping("/api/readStatuses")//이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService; //읽음 상태와 관련된 비즈니스 로직을 처리하는 서비스 객체

  @PostMapping("create") //새로운 읽음 상태를 생성하는 요청을 처리하는 메서드
  public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request) {
    ReadStatus createReadStatus = readStatusService.create(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createReadStatus);
  }

  @PatchMapping("/{readStatusId}") //기존 읽음상태를 수정하는 요청을 처리하는 메서드
  public ResponseEntity<ReadStatus> update(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request) {
    ReadStatus updateReadStatus = readStatusService.update(readStatusId, request);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updateReadStatus);
  }

  @GetMapping //특정 사용자의 모든 읽음 상태를 조회하는 요청을 처리하는 메서드
  public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam UUID userId) {
    List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(readStatuses);
  }
}
