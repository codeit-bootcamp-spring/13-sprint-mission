package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public interface BinaryContentStorage {
    UUID put(UUID id, byte[] content);
    InputStream get(UUID id) throws IOException;
    ResponseEntity<?> download(BinaryContentDto binaryContentDto);
    void delete(UUID id);
}
