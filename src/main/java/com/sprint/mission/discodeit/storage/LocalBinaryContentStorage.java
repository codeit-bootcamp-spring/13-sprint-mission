package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") Path root
    ) {
        this.root = root;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("파일 저장소 초기화에 실패했습니다.", e);
        }
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        try {
            Files.write(resolvePath(binaryContentId), bytes);
            return binaryContentId;
        } catch (Exception e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        try {
            return Files.newInputStream(resolvePath(binaryContentId));
        } catch (Exception e) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        InputStream inputStream = get(binaryContentDto.id());
        Resource resource = new InputStreamResource(inputStream);

        String encodedFileName = URLEncoder.encode(
                binaryContentDto.fileName(),
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_TYPE, binaryContentDto.contentType())
                .contentLength(binaryContentDto.size())
                .body(resource);
    }

    private Path resolvePath(UUID binaryContentId) {
        return root.resolve(binaryContentId.toString());
    }
}