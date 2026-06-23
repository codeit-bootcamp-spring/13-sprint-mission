package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/binary-contents")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 1. 바이너리 파일 1개 조회 및 다운로드 처리
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getBinaryContent(@PathVariable UUID binaryContentId) {
        BinaryContent content = binaryContentService.find(binaryContentId);

        // 파일명 브라우저 인코딩 및 컨텐트 타입 헤더 설정
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + content.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(content.getContentType()))
                .body(content.getBytes());
    }

    // 2. 바이너리 파일 여러 개 목록 조회 (쿼리 파라미터 ?ids=uuid1,uuid2... 로 바인딩 가능)
    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContent> getBinaryContentsIn(@RequestParam List<UUID> ids) {
        return binaryContentService.findAllByIdIn(ids);
    }
}