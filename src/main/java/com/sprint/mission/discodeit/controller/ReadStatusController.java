package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.docs.ReadStatusControllerDoc;
import com.sprint.mission.discodeit.dto.input.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.input.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/readStatuses")
public class ReadStatusController implements ReadStatusControllerDoc {

    private final ReadStatusService readStatusService;

    @RequestMapping(
            value = "",
            method = RequestMethod.GET
    )
    public ResponseEntity<List<ReadStatus>> findAllByUserId(
            @RequestParam(value = "userId") UUID userId
    ){
        List<ReadStatus> res = readStatusService.findAllByUserID(userId);
        return ResponseEntity.ok(res);
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST
    )
    public ResponseEntity<ReadStatus> create(
            @RequestBody ReadStatusCreateRequest rscr
    ){
        ReadStatus res = readStatusService.create(rscr);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatus> update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest rsur
            ){
        ReadStatus rs = readStatusService.update(readStatusId,rsur);
        return ResponseEntity.ok(rs);
    }
}
