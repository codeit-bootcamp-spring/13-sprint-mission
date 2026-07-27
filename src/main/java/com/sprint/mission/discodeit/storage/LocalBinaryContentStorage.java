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
            log.info("로컬 바이너리 스토리지 초기화 완료: {}", root.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("루트 디렉토리 초기화 실패했습니다.", e);
        }
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        Path path = resolvePath(binaryContentId);
        try {
            Files.write(path, bytes);
            return binaryContentId;
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 실패: " + binaryContentId, e);
        }
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
    public Resource getAsResource(BinaryContentDto dto) {
        Path path = resolvePath(dto.id());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다: " + dto.id());
        }
        return new FileSystemResource(path);
    }

    @Override
    public void delete(UUID binaryContentId) {
        Path path = resolvePath(binaryContentId);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 삭제 실패: " + binaryContentId, e);
        }
    }


}
