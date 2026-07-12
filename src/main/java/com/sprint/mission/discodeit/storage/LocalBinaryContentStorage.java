package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    // application.yaml의 discodeit.storage.local.root-path 값을 주입
    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath) {
        this.root = Paths.get(rootPath);
    }

    // Bean이 생성될 때 루트 디렉토리를 자동으로 초기화
    @PostConstruct
    public void init() {
        try {
            if (Files.notExists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("로컬 스토리지 루트 디렉토리 생성 실패: " + root, e);
        }
    }

    @Override
    public UUID put(UUID id, byte[] bytes) {
        Path filePath = resolvePath(id);
        try {
            Files.write(filePath, bytes);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + filePath, e);
        }
        return id;
    }

    @Override
    public InputStream get(UUID id) {
        Path filePath = resolvePath(id);
        try {
            if (Files.notExists(filePath)) {
                throw new NoSuchElementException("파일을 찾을 수 없습니다: " + id);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + filePath, e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        // get()으로 파일 읽어서 Resource로 감싸기
        InputStream inputStream = get(binaryContentDto.id());
        try {
            // InputStream을 임시 파일로 저장 후 UrlResource로 변환
            Path filePath = resolvePath(binaryContentDto.id());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new NoSuchElementException("파일을 찾을 수 없습니다: " + binaryContentDto.id());
            }

            // 파일 이름에 한글/특수문자가 있어도 깨지지 않도록 UTF-8 인코딩
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(binaryContentDto.fileName(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(MediaType.parseMediaType(binaryContentDto.contentType()))
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 URL 변환 실패", e);
        }
    }

    // 파일 저장 경로 규칙: {root}/{UUID}
    private Path resolvePath(UUID id) {
        return root.resolve(id.toString());
    }
}