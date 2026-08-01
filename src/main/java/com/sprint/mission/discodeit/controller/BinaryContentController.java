package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
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
    log.info("[파일 다운로드 요청] binaryContentId: {}", binaryContentId);

    BinaryContentDto binaryContentDto = binaryContentService.find(binaryContentId);
    ResponseEntity<?> response = binaryContentStorage.download(binaryContentDto);

    log.info("[파일 다운로드 완료] binaryContentId: {}, fileName: {}",
        binaryContentId, binaryContentDto.fileName());
    return response;
  }

}