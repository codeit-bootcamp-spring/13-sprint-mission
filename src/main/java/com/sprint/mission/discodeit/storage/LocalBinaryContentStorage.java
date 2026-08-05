package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.storage.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
@Slf4j
public class LocalBinaryContentStorage implements BinaryContentStorage{

    private final Path root;

    public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") String rootPath){
        this.root = Paths.get(rootPath);
    }

    // 폴더 생성
    @PostConstruct
    public void init(){
        try {
            Files.createDirectories(root);
        }catch (IOException e){
            log.error("스토리지 초기화 실패 - path: {}", root, e);
            throw StorageException.initFailed(root.toString());
        }
    }


    // 파일 저장
    @Override
    public UUID put(UUID id, byte[] bytes) {
        Path path = resolvePath(id);

        try {
            Files.write(path, bytes);
        } catch (IOException e){
            throw StorageException.putFailed(id);
        }
        return id;
    }


    @Override
    public InputStream get(UUID id) {
        Path path = resolvePath(id);

        if (!Files.exists(path)) {
            log.warn("파일 조회 실패 - 존재하지 않는 파일 id: {}", id);
            throw BinaryContentNotFoundException.withId(id);
        }

        try {
            return Files.newInputStream(path);
        }catch (IOException e){
            throw StorageException.getFailed(id);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        log.info("파일 다운로드 - Id: {}, fileName: {}", binaryContentDto.id(), binaryContentDto.fileName());
        InputStream inputStream = get(binaryContentDto.id());
        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + binaryContentDto.fileName() + "\"")
                .contentType(MediaType.parseMediaType(binaryContentDto.contentType()))
                .body(resource);
    }


    private Path resolvePath(UUID id){
        return root.resolve(id.toString());
    }


}
