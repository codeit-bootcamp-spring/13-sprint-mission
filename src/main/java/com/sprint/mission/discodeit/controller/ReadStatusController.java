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

@RequestMapping("/api/read-status")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;


    // 특정 채널의 메시지 수신 정보를 생성 api
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatusResponse> createReadStatus(@RequestBody ReadStatusCreateRequest request){
        ReadStatusResponse readStatusResponse = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusResponse);
    }

    //  특정 채널의 메시지 수신 정보를 수정 api
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatusResponse> ReadStatusUpdate(@PathVariable UUID id, @RequestBody ReadStatusUpdateRequest request){
        ReadStatusResponse update = readStatusService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(update);
    }

    // 특정 사용자의 메시지 수신 정보를 조회 api
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatusResponse>> getReadStatus(@RequestParam UUID userid){
        List<ReadStatusResponse> allByUserId = readStatusService.findAllByUserId(userid);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);

    }

}
