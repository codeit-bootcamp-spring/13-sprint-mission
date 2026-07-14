package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/reads-tatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @PostMapping
    public ReadStatusResponse create(
            @RequestBody CreateReadStatusRequest request) {
        return readStatusService.create(request);
    }

    @PatchMapping(value = "/{readStatusId}")
    public ReadStatusResponse update(
            @PathVariable UUID readStatusId,
            @RequestBody UpdateReadStatusRequest request
    ) {
        return readStatusService.update(readStatusId, request);
    }

    @GetMapping
    public List<ReadStatusResponse> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return readStatusService.findAllByUserId(userId);
    }
}
