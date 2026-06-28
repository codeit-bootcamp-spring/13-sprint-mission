package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
public interface ReadStatusApi {

  @Operation(summary = "Message 읽음 상태 생성")
  @PostMapping("create")
  ResponseEntity<ReadStatus> create(
      @RequestBody ReadStatusCreateRequest request
  );

  @Operation(summary = "Message 읽음 상태 수정")
  @PatchMapping("readStatusesId")
  ResponseEntity<ReadStatus> update(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request
  );

  @Operation(summary = "User의 Message 읽음 상태 목록 조회")
  @GetMapping
  ResponseEntity<List<ReadStatus>> findAllByUserId(
      @RequestParam UUID userId
  );
}
