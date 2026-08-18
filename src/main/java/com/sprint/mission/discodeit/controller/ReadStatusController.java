package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.docs.ReadStatusControllerDoc;
import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<ReadStatusDto>> findAllByUserId(
            @RequestParam(value = "userId") UUID userId
    ){
        List<ReadStatusDto> res = readStatusService.findAllByUserID(userId);
        return ResponseEntity.ok(res);
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST
    )
    public ResponseEntity<ReadStatusDto> create(
            @Valid @RequestBody ReadStatusCreateRequest rscr
    ){
        ReadStatusDto res = readStatusService.create(rscr);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatusDto> update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest rsur
            ){
        ReadStatusDto rs = readStatusService.update(readStatusId,rsur);
        return ResponseEntity.ok(rs);
    }
}
