package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.entity.BinaryContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "BinaryContent", description = "첨부 파일 API")
@RequestMapping("/api/binaryContents")
public interface BinaryContentApi {

  @Operation(summary = "첨부 파일 조회")
  @GetMapping("/api/binaryContentId")
  ResponseEntity<BinaryContent> find(
      @PathVariable UUID binaryContentId
  );

  @Operation(summary = "여러 첨부 파일 조회")
  @GetMapping
  ResponseEntity<List<BinaryContent>> findAllByIdIn(
      @RequestParam List<UUID> binaryContentIds
  );

}
