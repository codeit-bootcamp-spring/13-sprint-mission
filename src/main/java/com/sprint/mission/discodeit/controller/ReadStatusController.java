package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
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
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @PostMapping
    public ResponseEntity<ReadStatusResponse> createReadStatus(@Valid @RequestBody ReadStatusCreateRequest request) {
        ReadStatusResponse response = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> updateReadStatus(
            @PathVariable("readStatusId") UUID readStatusId,
            @Valid @RequestBody ReadStatusUpdateRequest request) {

        ReadStatusResponse response = readStatusService.update(readStatusId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReadStatusResponse>> getReadStatus(
            @RequestParam("userId") UUID userId) {

        List<ReadStatusResponse> responses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(responses);
    }

}
