package com.sprint.mission.discodeit.controller.docs;


import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
public interface ReadStatusControllerDocs {

    @Operation(summary = "Message 읽음 상태 생성 API")
    @PostMapping()
    public ResponseEntity<ReadStatusResponse> createReadStatus(
            @RequestBody ReadStatusCreateRequest request);

    @Operation(summary = "Message 읽음 상태 수정 API")
    @PatchMapping( "/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> ReadStatusUpdate(
            @PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request);

    @Operation(summary = "User Message 읽음 상태 목록 조회 API")
    @GetMapping()
    public ResponseEntity<List<ReadStatusResponse>> getReadStatus(@RequestParam UUID userId);
}
