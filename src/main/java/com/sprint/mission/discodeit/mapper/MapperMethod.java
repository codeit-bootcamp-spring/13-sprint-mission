package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class MapperMethod {
    private final BinaryContentStorage binaryContentStorage;

    public byte[] getByteFrom(UUID id) {
        try (InputStream in = binaryContentStorage.get(id)){
            return in.readAllBytes();
        } catch (IOException e) {
            log.error("read data error" + id.toString(), e);
            return null;
        }
    }
}
