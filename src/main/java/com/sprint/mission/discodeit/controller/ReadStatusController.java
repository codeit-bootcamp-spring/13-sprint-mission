package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatus")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    //특정 채널의 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createReadStatus(@Valid @RequestBody ReadStatusCreateRequest request) {
        readStatusService.createReadStatus(request);

        return ResponseEntity.ok().build();
    }

    //특정 사용자의 메시지 수신 정보 조회
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public List<ReadStatus> findReadStatusByUser(@PathVariable UUID userId) {
        List<ReadStatus> responseList = readStatusService.findAllReadStatusByUserId(userId);

        return responseList;
    }

    //특정 채널의 메시지 수신 정보 수정
    @RequestMapping(method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatusUpdateResponse> updateReadStatus(@Valid @RequestBody ReadStatusUpdateRequest request) {
        ReadStatusUpdateResponse response = readStatusService.updateReadStatus(request);

        return ResponseEntity.ok().body(response);
    }

}
