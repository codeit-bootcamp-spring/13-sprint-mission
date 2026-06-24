package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @Operation(summary = "Message 읽음 상태 생성 API")
    @PostMapping()
    public ResponseEntity<ReadStatusResponse> createReadStatus(@RequestBody ReadStatusCreateRequest request){
        ReadStatusResponse readStatusResponse = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusResponse);
    }

    @Operation(summary = "Message 읽음 상태 수정 API")
    @PatchMapping( "/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> ReadStatusUpdate(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request){
        ReadStatusResponse update = readStatusService.update(readStatusId, request);
        return ResponseEntity.status(HttpStatus.OK).body(update);
    }

    @Operation(summary = "User Message 읽음 상태 목록 조회 API")
    @GetMapping()
    public ResponseEntity<List<ReadStatusResponse>> getReadStatus(@RequestParam UUID userId){
        List<ReadStatusResponse> allByUserId = readStatusService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);

    }

}
