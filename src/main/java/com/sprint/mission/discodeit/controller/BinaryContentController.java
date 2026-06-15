package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/binary-contents")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 바이너리 파일 다운로드
    // [ ] 바이너리 파일을 1개 또는 여러 개 조회할 수 있다.

    // 단건 조회
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BinaryContentResponse getBinaryContentById(@PathVariable UUID id) {
        return binaryContentService.find(id)
                .orElseThrow(() -> new DiscodeitException.FileNotFoundException("해당 파일을 찾을 수 없습니다."));
    }

    // 다건 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentResponse>  getBinaryContents
            (@RequestParam(required = false) List<UUID> ids) {
        if (ids != null && !ids.isEmpty()) {
            return binaryContentService.findAllByIdIn(ids);
        }
        return List.of();
    }

}
