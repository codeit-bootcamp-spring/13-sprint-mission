package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "BinaryContent", description = "첨부 파일 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // GET /api/binaryContents/{binaryContentId} -> 첨부 파일 1개 조회
  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContent> find(@PathVariable("binaryContentId") UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentService.find(binaryContentId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(binaryContent);
  }

  // GET /api/binaryContents?binaryContentIds=&binaryContentIds= -> 여러 첨부 파일 조회
  @GetMapping
  public ResponseEntity<List<BinaryContent>> findAllByIdIn(
          @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    List<BinaryContent> binaryContents = binaryContentService.findAllByIdIn(binaryContentIds);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(binaryContents);
  }
}