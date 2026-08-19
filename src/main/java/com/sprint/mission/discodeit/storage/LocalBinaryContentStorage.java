package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "local",
        matchIfMissing = true
)
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path rootPath;

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath
    ) {
        this.rootPath = Path.of(rootPath);
    }

    @Override
    public void put(UUID id, byte[] bytes) {
        try {
            Files.createDirectories(rootPath);

            Path filePath = rootPath.resolve(id.toString());

            Files.write(filePath, bytes);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "파일 저장에 실패했습니다: " + id,
                    exception
            );
        }
    }

    @Override
    public ResponseEntity<?> download(
            BinaryContentDto binaryContentDto
    ) {
        try {
            Path filePath = rootPath.resolve(
                    binaryContentDto.id().toString()
            );

            Resource resource = new UrlResource(
                    filePath.toUri()
            );

            if (!resource.exists()) {
                throw new IllegalArgumentException(
                        "저장된 파일을 찾을 수 없습니다: "
                                + binaryContentDto.id()
                );
            }

            ContentDisposition disposition =
                    ContentDisposition.attachment()
                            .filename(
                                    binaryContentDto.fileName(),
                                    StandardCharsets.UTF_8
                            )
                            .build();

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    binaryContentDto.contentType()
                            )
                    )
                    .contentLength(binaryContentDto.size())
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposition.toString()
                    )
                    .body(resource);

        } catch (MalformedURLException exception) {
            throw new RuntimeException(
                    "파일 다운로드에 실패했습니다: "
                            + binaryContentDto.id(),
                    exception
            );
        }
    }
}