package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 바이너리 파일 다운로드
    // [ ] 바이너리 파일을 1개 또는 여러 개 조회할 수 있다.

    // 단건 조회
    @GetMapping("/{binaryContentId}") // 💡 스펙의 경로 변수명({binaryContentId})과 일치시켰습니다.
    public ResponseEntity<BinaryContentResponse> getBinaryContentById(@PathVariable UUID binaryContentId) {
        BinaryContentResponse response = binaryContentService.find(binaryContentId)
                .orElseThrow(() -> new DiscodeitException.FileNotFoundException("해당 파일을 찾을 수 없습니다."));
        return ResponseEntity.ok(response);
    }

    // 다건 조회
    @GetMapping
    public ResponseEntity<List<BinaryContentResponse>> getBinaryContents(
            @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {

        if (binaryContentIds != null && !binaryContentIds.isEmpty()) {
            return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
        }
        return ResponseEntity.ok(List.of());
    }

}
