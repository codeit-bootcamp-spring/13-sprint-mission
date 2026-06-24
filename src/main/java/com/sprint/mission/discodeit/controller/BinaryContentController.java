package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
@ResponseBody
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // 바이너리 파일을 1개 또는 여러 개 조회
  @RequestMapping(path = "/{contentId}", method = RequestMethod.GET)
  public ResponseEntity<BinaryContentResponse> find(@PathVariable UUID contentId) {
    BinaryContentResponse foundContent = binaryContentService.find(contentId);
    return ResponseEntity.status(HttpStatus.OK).body(foundContent);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
      @RequestParam(required = false) List<UUID> contentIds) {
    List<BinaryContentResponse> foundAllByIdIn = binaryContentService.findAllByIdIn(contentIds);
    return ResponseEntity.status(HttpStatus.OK).body(foundAllByIdIn);
  }
}
