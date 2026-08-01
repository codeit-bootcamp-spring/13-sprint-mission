package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private static final Logger log =
            LoggerFactory.getLogger(BinaryContentController.class);

    private final BinaryContentService binaryContentService;

    @PostMapping
    public BinaryContentDto create(
            @RequestPart("file") MultipartFile file
    ) {

        log.debug(
                "파일 업로드 요청: fileName={}, size={} bytes",
                file.getOriginalFilename(),
                file.getSize()
        );

        BinaryContentDto dto = binaryContentService.create(file);

        log.info("파일 업로드 완료");

        return dto;
    }

    @GetMapping("/{binaryContentId}")
    public BinaryContentDto find(
            @PathVariable UUID binaryContentId
    ) {
        log.debug(
                "파일 조회 요청: binaryContentId={}",
                binaryContentId
        );

        return binaryContentService.find(binaryContentId);
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(
            @PathVariable UUID binaryContentId
    ) {
        log.debug(
                "파일 다운로드 요청: binaryContentId={}",
                binaryContentId
        );

        return binaryContentService.download(binaryContentId);
    }
}