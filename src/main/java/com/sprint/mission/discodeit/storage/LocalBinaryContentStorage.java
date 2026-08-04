package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.exception.file.FileStorageException;
import com.sprint.mission.discodeit.exception.file.FolderCreateException;
import com.sprint.mission.discodeit.exception.file.StoredFileNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(
        value = "discodeit.storage.type",
        havingValue = "local"
)
public class LocalBinaryContentStorage implements BinaryContentStorage{

    //필드
    private final Path root;

    public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") Path root) {
        this.root = root;
    }

    @PostConstruct
    public void init(){
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            log.error("폴더 생성에 실패했습니다.");
            throw new FolderCreateException(root.toString());
        }
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        Path path = resolvePath(binaryContentId);

        //파일 저장
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            log.error("파일 저장에 실패했습니다.");
            throw new FileStorageException(binaryContentId);
        }

        return binaryContentId;
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        Path path = resolvePath(binaryContentId);

        try {
            if (!Files.exists(path)) {
                throw new StoredFileNotFoundException(binaryContentId);
            }

            return Files.newInputStream(path);
        } catch (IOException e) {
            log.error("파일 읽기에 실패했습니다.");
            throw new FileStorageException(binaryContentId);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        log.debug("파일 다운로드 시작");

        InputStream inputStream = get(binaryContentDto.id());
        Resource resource = new InputStreamResource(inputStream);

        log.info("파일 다운로드 완료");

        return ResponseEntity.ok().body(resource);
    }

    private Path resolvePath(UUID binaryContentId) {
        return root.resolve(binaryContentId.toString());
    }
}
