package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @PostMapping
    public ResponseEntity<ReadStatusDto> create(@RequestBody ReadStatusCreateRequest request) {
        return ResponseEntity.ok(readStatusService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }

    @PatchMapping(value = "/{readStatusId}")
    public ResponseEntity<ReadStatusDto> update(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request) {
        return ResponseEntity.ok(readStatusService.update(request));
    }

}
