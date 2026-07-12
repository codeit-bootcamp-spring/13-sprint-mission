package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContent binaryContent =
                binaryContentRepository.findById(binaryContentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "BinaryContent를 찾을 수 없습니다: "
                                                + binaryContentId
                                )
                        );

        BinaryContentDto binaryContentDto =
                binaryContentMapper.toDto(binaryContent);

        return binaryContentStorage.download(binaryContentDto);
    }
}