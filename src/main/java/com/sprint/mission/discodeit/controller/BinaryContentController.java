package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary-content")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;


    //BinaryContent 조회 api
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentResponse>>findAllByUserId(@RequestParam List<UUID> ids) {
        List<BinaryContentResponse> allByIdIn = binaryContentService.findAllByIdIn(ids);
        return ResponseEntity.status(HttpStatus.OK).body(allByIdIn);
    }
}
