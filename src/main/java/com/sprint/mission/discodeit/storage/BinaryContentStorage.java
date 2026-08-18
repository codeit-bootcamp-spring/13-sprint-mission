package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface BinaryContentStorage {

    void put(UUID id, byte[] bytes);

    byte[] get(UUID id);

    void delete(UUID id);

    ResponseEntity<?> download(
            UUID id,
            BinaryContentDownloadResponse response
    );
}