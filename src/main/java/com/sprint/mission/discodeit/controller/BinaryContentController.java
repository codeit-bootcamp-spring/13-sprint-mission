package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  @GetMapping("/{binaryContentId}")
  public BinaryContentDto find(
      @PathVariable("binaryContentId") UUID id
  ) {
    log.debug("파일 정보 조회 요청: binaryContentId={}", id);

    BinaryContentDto response = binaryContentService.find(id);

    log.debug("파일 정보 조회 응답 완료: binaryContentId={}", id);

    return response;
  }

  @GetMapping
  public List<BinaryContentDto> findByIdIn(
      @RequestParam("binaryContentIds") List<UUID> ids
  ) {
    log.debug("파일 정보 목록 조회 요청: requestedCount={}", ids.size());

    List<BinaryContentDto> response =
        binaryContentService.findByIdIn(ids);

    log.debug(
        "파일 정보 목록 조회 응답 완료: requestedCount={}, resultCount={}",
        ids.size(),
        response.size()
    );

    return response;
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(
      @PathVariable("binaryContentId") UUID id
  ) {
    log.debug("파일 다운로드 요청: binaryContentId={}", id);

    return binaryContentService.download(id);
  }
}
