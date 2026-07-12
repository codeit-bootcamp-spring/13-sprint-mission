package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "local"
)
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath
    ) {
        this.root = Path.of(rootPath);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "로컬 저장소 디렉토리를 생성할 수 없습니다: " + root,
                    e
            );
        }
    }

    protected Path resolvePath(UUID id) {
        return root.resolve(id.toString());
    }

    @Override
    public UUID put(UUID id, byte[] bytes) {
        if (id == null) {
            throw new IllegalArgumentException("BinaryContent id는 필수입니다.");
        }

        if (bytes == null) {
            throw new IllegalArgumentException("저장할 바이너리 데이터는 필수입니다.");
        }

        Path path = resolvePath(id);

        try {
            Files.write(
                    path,
                    bytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return id;
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "파일 저장에 실패했습니다: " + id,
                    e
            );
        }
    }

    @Override
    public InputStream get(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("BinaryContent id는 필수입니다.");
        }

        Path path = resolvePath(id);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "저장된 파일을 찾을 수 없습니다: " + id
            );
        }

        try {
            return Files.newInputStream(
                    path,
                    StandardOpenOption.READ
            );
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "파일을 읽을 수 없습니다: " + id,
                    e
            );
        }
    }

    @Override
    public ResponseEntity<Resource> download(
            BinaryContentDto binaryContentDto
    ) {
        if (binaryContentDto == null) {
            throw new IllegalArgumentException(
                    "BinaryContentDto는 필수입니다."
            );
        }

        InputStream inputStream = get(binaryContentDto.id());
        Resource resource = new InputStreamResource(inputStream);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(
                    binaryContentDto.contentType()
            );
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                binaryContentDto.fileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(binaryContentDto.size())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(resource);
    }
}