package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.*;
import jakarta.annotation.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.core.io.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
        } catch (IOException e) {
            throw new UncheckedIOException("루트 디렉토리 초기화 실패했습니다.", e);
        }
    }
    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        return null;
    }

    private Path resolvePath(UUID binaryContentId) {
        return root.resolve(binaryContentId.toString());
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        Path path = resolvePath(binaryContentId);
        try{
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 실패", e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentResponse response) {
        InputStream inputStream = get(response.id());
        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.fileName() + "\"")
                .contentType(MediaType.parseMediaType(response.contentType()))
                .body(resource);
    }


}
