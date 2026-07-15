package com.sprint.mission.discodeit.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
            @Value("${discodeit.storage.local.root-path:data/binaryContents}") String rootPath
    ) {
        this.rootPath = Path.of(rootPath);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("바이너리 콘텐츠 저장소 디렉토리 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void put(UUID id, byte[] bytes) {
        if (id == null) {
            throw new IllegalArgumentException("저장할 바이너리 콘텐츠 id는 필수입니다.");
        }

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("저장할 바이너리 데이터는 비어 있을 수 없습니다.");
        }

        try {
            Files.write(resolvePath(id), bytes);
        } catch (IOException e) {
            throw new RuntimeException("바이너리 콘텐츠 파일 저장 중 오류가 발생했습니다. id=" + id, e);
        }
    }

    @Override
    public byte[] get(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("조회할 바이너리 콘텐츠 id는 필수입니다.");
        }

        Path path = resolvePath(id);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("바이너리 콘텐츠 파일을 찾을 수 없습니다. id=" + id);
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("바이너리 콘텐츠 파일 조회 중 오류가 발생했습니다. id=" + id, e);
        }
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("삭제할 바이너리 콘텐츠 id는 필수입니다.");
        }

        try {
            Files.deleteIfExists(resolvePath(id));
        } catch (IOException e) {
            throw new RuntimeException("바이너리 콘텐츠 파일 삭제 중 오류가 발생했습니다. id=" + id, e);
        }
    }

    private Path resolvePath(UUID id) {
        return rootPath.resolve(id.toString());
    }
}