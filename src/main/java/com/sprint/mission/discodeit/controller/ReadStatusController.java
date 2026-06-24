package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/read-status")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatusResponse> create(
            @RequestBody ReadStatusCreateRequest request
    ) {
        return ResponseEntity.ok(readStatusService.create(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<ReadStatusResponse>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<ReadStatusResponse> update(
            @RequestBody ReadStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(readStatusService.update(request));
    }
}
