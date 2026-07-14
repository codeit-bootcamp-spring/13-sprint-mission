package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
  public ResponseEntity<BinaryContentDto> find(@PathVariable UUID binaryContentId) {
    BinaryContentDto findBinaryContent = binaryContentService.find(binaryContentId);
    return ResponseEntity.status(HttpStatus.OK).body(findBinaryContent);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
      @RequestParam List<UUID> binaryContentIds) {
    List<BinaryContentDto> finaAllBinaryContentIdIn = binaryContentService.findAllByIdIn(
        binaryContentIds);
    return ResponseEntity.status(HttpStatus.OK).body(finaAllBinaryContentIdIn);
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    BinaryContentDto binaryContentDto = binaryContentService.find(binaryContentId);

    return binaryContentStorage.download(binaryContentDto);
  }

}
