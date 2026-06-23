package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    //특정 채널의 메시지 수신 정보 생성
    @Operation(summary = "Message 읽음 상태 생성")
    @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> createReadStatus(@Valid @RequestBody ReadStatusCreateRequest request) {
        ReadStatus createdReadStatus = readStatusService.createReadStatus(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdReadStatus);
    }

    //특정 사용자의 메시지 수신 정보 조회
    @Operation(summary = "User의 Message 읽음 상태 목록 조회")
    @ApiResponse(responseCode = "200", description = "Message 읽음 상태 목록 조회 성공")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> findAllByUserId(@Parameter(description = "조회할 User ID", required = true)
                                                            @RequestParam UUID userId) {
        List<ReadStatus> responseList = readStatusService.findAllReadStatusByUserId(userId);

        return ResponseEntity.ok().body(responseList);
    }

    //특정 채널의 메시지 수신 정보 수정
    @Operation(summary = "Message 읽음 상태 수정")
    @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨")
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatus> updateReadStatus(@Parameter(description = "수정할 읽음 상태 ID", required = true)
                                                                     @PathVariable UUID readStatusId,
                                                                     @Valid @RequestBody ReadStatusUpdateRequest request) {
        ReadStatus response = readStatusService.updateReadStatus(readStatusId, request);

        return ResponseEntity.ok().body(response);
    }

}
