package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import jakarta.annotation.PostConstruct;
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
        }catch (Exception e){
            throw new RuntimeException("폴더 생성 실패", e);
        }
    }


    // 파일 저장
    @Override
    public UUID put(UUID id, byte[] bytes) {
        Path path = resolvePath(id);

        try {
            Files.write(path, bytes);
        } catch (IOException e){
            throw new RuntimeException("파일 저장에 실패했습니다.");
        }
        return id;
    }


    @Override
    public InputStream get(UUID id) {
        Path path = resolvePath(id);

        try {
            if (!Files.exists(path)) {
                throw new RuntimeException("파일이 존재하지 않습니다.");
            }
            return Files.newInputStream(path);
        }catch (IOException e){
            throw new RuntimeException("파일 불러오기 실패");
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
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
