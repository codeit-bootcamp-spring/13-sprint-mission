package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @PostMapping
    public ResponseEntity<ReadStatusDto> create(
           @Valid @RequestBody CreateReadStatusRequest request) {
        ReadStatusDto dto = readStatusService.create(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PatchMapping(value = "/{readStatusId}")
    public ResponseEntity<ReadStatusDto> update(
            @PathVariable UUID readStatusId,
            @Valid @RequestBody UpdateReadStatusRequest request
    ) {
        ReadStatusDto dto = readStatusService.update(readStatusId, request.toCommand());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ReadStatusDto>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        List<ReadStatusDto> readStatuses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatuses);
    }
}
