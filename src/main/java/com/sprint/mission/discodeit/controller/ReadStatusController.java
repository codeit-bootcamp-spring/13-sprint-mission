package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatusResponse> create(
            @RequestBody ReadStatusCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusService.create(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<ReadStatusResponse>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatusResponse> update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(readStatusService.update(readStatusId, request));
    }
}
