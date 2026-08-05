package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface BinaryContentStorage {

    void put(UUID id, byte[] bytes);

    ResponseEntity<?> download(BinaryContentDto binaryContentDto);
}