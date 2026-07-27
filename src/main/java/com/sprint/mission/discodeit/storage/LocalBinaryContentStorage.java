package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.*;
import jakarta.annotation.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import static org.springframework.http.MediaType.parseMediaType;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") Path root) {
        this.root = root;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
            log.info("로컬 바이너리 스토리지 초기화 완료: {}", root.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("루트 디렉토리 초기화 실패했습니다.", e);
        }
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        if (binaryContentId == null) {
            throw new IllegalArgumentException(
                    "바이너리 콘텐츠 ID는 필수입니다."
            );
        }

        if (bytes == null) {
            throw new IllegalArgumentException(
                    "저장할 바이너리 데이터는 필수입니다."
            );
        }
        Path path = resolvePath(binaryContentId);
        try {
            Files.write(path, bytes);
            return binaryContentId;
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 실패: " + binaryContentId, e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        if (binaryContentDto == null) {
            throw new IllegalArgumentException(
                    "다운로드할 바이너리 콘텐츠 정보는 필수입니다."
            );
        }

        InputStream inputStream = get(binaryContentDto.id());
        InputStreamResource resource = new InputStreamResource(inputStream);

        MediaType mediaType = parseMediaType(binaryContentDto.contentType());

        ContentDisposition contentDisposition = ContentDisposition.attachment()
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

    private Path resolvePath(UUID binaryContentId) {
        return root.resolve(binaryContentId.toString());
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        if (binaryContentId == null) {
            throw new IllegalArgumentException(
                    "바이너리 콘텐츠 ID는 필수입니다."
            );
        }
        Path path = resolvePath(binaryContentId);
        try{
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 실패", e);
        }
    }

}
