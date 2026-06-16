package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.input.QueryBinaryInput;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.basic.BasicBinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping({"/api/binaryContent","/api/v1/binaryContent"} )
public class BinaryFileController {

    private final BasicBinaryContentService bbcs;

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public BinaryContent findByID(
//            @RequestBody QueryBinaryInput qbi
            @RequestParam(value = "binaryContentId") UUID id
            ){
        return bbcs.find(id);
    }

}
