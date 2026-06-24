package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;


    // 특정 채널의 메시지 수신 정보를 생성 api
    @PostMapping()
    public ResponseEntity<ReadStatusResponse> createReadStatus(@RequestBody ReadStatusCreateRequest request){
        ReadStatusResponse readStatusResponse = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusResponse);
    }

    //  특정 채널의 메시지 수신 정보를 수정 api
    @PatchMapping( "/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> ReadStatusUpdate(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request){
        ReadStatusResponse update = readStatusService.update(readStatusId, request);
        return ResponseEntity.status(HttpStatus.OK).body(update);
    }

    // 특정 사용자의 메시지 수신 정보를 조회 api
    @GetMapping()
    public ResponseEntity<List<ReadStatusResponse>> getReadStatus(@RequestParam UUID userId){
        List<ReadStatusResponse> allByUserId = readStatusService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);

    }

}
