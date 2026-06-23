package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.basic.BasicBinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping({"/api/binaryContents"} )
public class BinaryFileController {

    private final BasicBinaryContentService bbcs;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findAll(
            @RequestParam List<UUID> binryContentIds
            ){
        return ResponseEntity.ok(bbcs.findAllByIdIn(binryContentIds));
    }

    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> find(
            @PathVariable UUID binaryContentId
            ){
        return ResponseEntity.ok(bbcs.findByID(binaryContentId));
    }

}
