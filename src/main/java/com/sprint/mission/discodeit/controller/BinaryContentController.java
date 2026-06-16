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
@RequestMapping("/binary-content")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BinaryContentResponse find(@PathVariable UUID id) {
        return binaryContentService.find(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentResponse> findByIdIn(@RequestParam List<UUID> ids) {
        return binaryContentService.findByIdIn(ids);
    }
}
