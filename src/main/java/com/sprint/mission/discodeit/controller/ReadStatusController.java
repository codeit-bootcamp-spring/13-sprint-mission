package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusResponse create(
            @RequestBody CreateReadStatusRequest request) {
        return readStatusService.create(request);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ReadStatusResponse update(
            @PathVariable UUID readStatusId,
            @RequestBody UpdateReadStatusRequest request
    ) {
        return readStatusService.update(readStatusId, request);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ReadStatusResponse> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return readStatusService.findAllByUserId(userId);
    }
}
