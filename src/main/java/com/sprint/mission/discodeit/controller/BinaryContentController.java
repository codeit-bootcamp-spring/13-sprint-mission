package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  @GetMapping("/{binaryContentId}")
  public BinaryContentResponse find(@PathVariable("binaryContentId") UUID id) {
    return binaryContentService.find(id);
  }

  @GetMapping
  public List<BinaryContentResponse> findByIdIn(@RequestParam("binaryContentIds") List<UUID> ids) {
    return binaryContentService.findByIdIn(ids);
  }
}
