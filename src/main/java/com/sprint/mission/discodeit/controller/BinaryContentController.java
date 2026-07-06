package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.docs.BinaryContentControllerDoc;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
public class BinaryContentController implements BinaryContentControllerDoc {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentDto>> findAll(
            @RequestParam List<UUID> binryContentIds
            ){
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(binryContentIds));
    }

    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentDto> find(
            @PathVariable UUID binaryContentId
            ){
        return ResponseEntity.ok(binaryContentService.findByID(binaryContentId));
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(@PathVariable UUID binaryContentId){
        return binaryContentStorage.download(
                binaryContentService.findByID(binaryContentId)
        );
    }

}
