package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.ReadStatusControllerDocs;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController implements ReadStatusControllerDocs {

    private final ReadStatusService readStatusService;

    @PostMapping()
    public ResponseEntity<ReadStatusResponse> createReadStatus(@RequestBody ReadStatusCreateRequest request){
        ReadStatusResponse readStatusResponse = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusResponse);
    }

    @PatchMapping( "/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> ReadStatusUpdate(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request){
        ReadStatusResponse update = readStatusService.update(readStatusId, request);
        return ResponseEntity.status(HttpStatus.OK).body(update);
    }

    @GetMapping()
    public ResponseEntity<List<ReadStatusResponse>> getReadStatus(@RequestParam UUID userId){
        List<ReadStatusResponse> allByUserId = readStatusService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);

    }

}
