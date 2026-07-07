package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.ReadStatusControllerDocs;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
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
    public ResponseEntity<ReadStatusDto> createReadStatus(@RequestBody ReadStatusCreateRequest request){
        ReadStatusDto readStatusDto = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusDto);
    }

    @PatchMapping( "/{readStatusId}")
    public ResponseEntity<ReadStatusDto> ReadStatusUpdate(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request){
        ReadStatusDto update = readStatusService.update(readStatusId, request);
        return ResponseEntity.status(HttpStatus.OK).body(update);
    }

    @GetMapping()
    public ResponseEntity<List<ReadStatusDto>> getReadStatus(@RequestParam UUID userId){
        List<ReadStatusDto> allByUserId = readStatusService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allByUserId);

    }

}
