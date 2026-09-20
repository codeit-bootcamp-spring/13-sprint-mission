package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BinaryContentService {

    BinaryContentDto create(MultipartFile file);

    BinaryContentDto find(UUID id);

    ResponseEntity<?> download(UUID id);
}