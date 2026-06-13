package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContent")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    //바이너리 파일을 1개 조회
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BinaryContent findBinaryContent(@PathVariable UUID id) {
        BinaryContent binaryContent = binaryContentService.findBinaryContentById(id);

        return binaryContent;
    }

    //바이너리 파일을 여러 개 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContent> findBinaryContent(@RequestParam List<UUID> binaryContentIds) {
        List<BinaryContent> binaryContents = binaryContentService.findAllBinaryContentByIdIn(binaryContentIds);

        return binaryContents;
    }


}
