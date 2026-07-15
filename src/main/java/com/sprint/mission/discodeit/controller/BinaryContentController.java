package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // 바이너리 파일 1개 조회
  @RequestMapping(
      value = "/{binaryContentId}",
      method = RequestMethod.GET
  )
  public Object find(
      @PathVariable UUID binaryContentId
  ) {

    return binaryContentService.find(binaryContentId);
  }

  // 바이너리 파일 여러 개 조회
  @RequestMapping(method = RequestMethod.GET)
  public Object findAllByIdIn(
      @RequestParam List<UUID> binaryContentIds
  ) {

    return binaryContentService.findAllByIdIn(binaryContentIds);
  }
}