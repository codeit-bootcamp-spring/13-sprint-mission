package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.docs.BinaryContentControllerDocs;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentControllerDocs {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContentDto> findById(@PathVariable UUID binaryContentId) {
        BinaryContentDto content = binaryContentService.find(binaryContentId);
        return ResponseEntity.status(HttpStatus.OK).body(content);
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
        BinaryContentDto binaryContentDto = binaryContentService.find(binaryContentId);
        return binaryContentStorage.download(binaryContentDto);
    }


    @GetMapping()
    public ResponseEntity<List<BinaryContentDto>> findAllByUserId(@RequestParam List<UUID> binaryContentIds) {
        List<BinaryContentDto> allByIdIn = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.status(HttpStatus.OK).body(allByIdIn);
    }
}
