package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/binary-contents")
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;  // 추가
  private final BinaryContentMapper binaryContentMapper;    // 추가

  @GetMapping(path = "{binaryContentId}")
  public ResponseEntity<BinaryContent> find(
          @PathVariable("binaryContentId") UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentService.find(binaryContentId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(binaryContent);
  }

  @GetMapping
  public ResponseEntity<List<BinaryContent>> findAllByIdIn(
          @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    List<BinaryContent> binaryContents = binaryContentService.findAllByIdIn(binaryContentIds);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(binaryContents);
  }

  @GetMapping(path = "{binaryContentId}/download")
  public ResponseEntity<Resource> download(
          @PathVariable("binaryContentId") UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentService.find(binaryContentId);
    // Entity → DTO 변환 후 Storage에 다운로드 위임
    BinaryContentDto dto = binaryContentMapper.toDto(binaryContent);
    return binaryContentStorage.download(dto);
  }
}