package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class MapperMethod {
    private final BinaryContentStorage binaryContentStorage;

    public byte[] getByteFrom(BinaryContent bc) {
        try (InputStream in = binaryContentStorage.get(bc.getId())){
            return in.readAllBytes();
        } catch (IOException e) {
            log.error("read data error" + bc.getId().toString(), e);
            return null;
        }
    }
}
