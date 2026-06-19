package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readstatus")
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> create( @RequestBody ReadStatusCreateRequest readStatusCreateRequest) {
        ReadStatus readStatus = readStatusService.create(readStatusCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatus);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.GET)
    public ResponseEntity<ReadStatus> find(@PathVariable UUID readStatusId){
        ReadStatus readStatus = readStatusService.find(readStatusId);
        return ResponseEntity.status(HttpStatus.OK).body(readStatus);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam UUID userId){
        List<ReadStatus> findAllByUserId = readStatusService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(findAllByUserId);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatus> update(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest readStatusUpdateRequest){
        ReadStatus readStatusUpdate = readStatusService.update(readStatusId,readStatusUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(readStatusUpdate);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID readStatusId){
        readStatusService.delete(readStatusId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
