package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Component
@RequiredArgsConstructor
public class BinaryContentMapper {

    private final BinaryContentStorage binaryContentStorage;

    public BinaryContentResponse toDto(BinaryContent binaryContent) {
        if (binaryContent == null) {
            return null;
        }

        byte[] bytes = readBytes(binaryContent);

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getSize(),
                binaryContent.getContentType(),
                bytes
        );
    }

    private byte[] readBytes(BinaryContent binaryContent) {
        try (InputStream inputStream = binaryContentStorage.get(binaryContent.getId())) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("바이너리 파일을 DTO로 변환할 수 없습니다.", e);
        }
    }

}